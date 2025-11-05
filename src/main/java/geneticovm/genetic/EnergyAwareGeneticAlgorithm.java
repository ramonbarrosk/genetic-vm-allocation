package geneticovm.genetic;

import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.vms.Vm;

import java.util.*;

/**
 * Algoritmo Genético para alocação de VMs baseado em economia de energia.
 * Implementa: Inicialização, Avaliação (Fitness), Seleção por Torneio,
 * Crossover Uniforme, Mutação e Condição de Término.
 */
public class EnergyAwareGeneticAlgorithm {
    
    private final List<Vm> vms;
    private final List<Host> hosts;
    private final int populationSize;
    private final int maxGenerations;
    private final double crossoverRate;
    private final double mutationRate;
    private final int tournamentSize;
    private final Random random;
    
    // Matriz de comunicação entre VMs (simplificada - todas se comunicam)
    private final double[][] communicationMatrix;
    
    public EnergyAwareGeneticAlgorithm(List<Vm> vms, List<Host> hosts, 
                                     int populationSize, int maxGenerations,
                                     double crossoverRate, double mutationRate,
                                     int tournamentSize, long seed) {
        this.vms = new ArrayList<>(vms);
        this.hosts = new ArrayList<>(hosts);
        this.populationSize = populationSize;
        this.maxGenerations = maxGenerations;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.tournamentSize = tournamentSize;
        this.random = new Random(seed);
        
        // Inicializar matriz de comunicação (simplificada)
        this.communicationMatrix = initializeCommunicationMatrix();
    }
    
    /**
     * Inicializa matriz de comunicação entre VMs
     */
    private double[][] initializeCommunicationMatrix() {
        int n = vms.size();
        double[][] matrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    matrix[i][j] = 0.0;
                } else {
                    // Comunicação aleatória entre 0.1 e 1.0
                    matrix[i][j] = 0.1 + random.nextDouble() * 0.9;
                }
            }
        }
        return matrix;
    }
    
    /**
     * Executa o algoritmo genético
     */
    public AllocationSolution run() {
        // 1. Inicialização da população
        List<AllocationSolution> population = initializePopulation();
        
        AllocationSolution bestSolution = null;
        double bestFitness = Double.MAX_VALUE;
        
        System.out.println("🧬 Executando Algoritmo Genético...");
        System.out.printf("  População: %d, Gerações: %d%n", populationSize, maxGenerations);
        
        // Avaliar população inicial
        for (AllocationSolution individual : population) {
            double fitness = evaluateFitness(individual);
            individual.setFitness(fitness);
            if (fitness < bestFitness) {
                bestFitness = fitness;
                bestSolution = new AllocationSolution(individual);
            }
        }
        
        // Evolução
        for (int generation = 0; generation < maxGenerations; generation++) {
            List<AllocationSolution> newPopulation = new ArrayList<>();
            
            // Elitismo: manter o melhor indivíduo
            newPopulation.add(new AllocationSolution(bestSolution));
            
            // Criar novos indivíduos
            while (newPopulation.size() < populationSize) {
                // Seleção por torneio
                AllocationSolution parent1 = tournamentSelection(population);
                AllocationSolution parent2 = tournamentSelection(population);
                
                // Crossover uniforme
                AllocationSolution child = uniformCrossover(parent1, parent2);
                
                // Mutação
                if (random.nextDouble() < mutationRate) {
                    mutate(child);
                }
                
                // Avaliar fitness do filho
                double fitness = evaluateFitness(child);
                child.setFitness(fitness);
                
                newPopulation.add(child);
                
                // Atualizar melhor solução
                if (fitness < bestFitness) {
                    bestFitness = fitness;
                    bestSolution = new AllocationSolution(child);
                }
            }
            
            population = newPopulation;
            
            if (generation % 5 == 0 || generation == maxGenerations - 1) {
                System.out.printf("  Geração %d: Melhor Fitness = %.4f, Hosts Ativos = %d%n",
                    generation + 1, bestFitness, bestSolution.getActiveHostsCount());
            }
        }
        
        System.out.println("✅ Algoritmo Genético concluído!\n");
        return bestSolution;
    }
    
    /**
     * Inicializa população aleatória
     */
    private List<AllocationSolution> initializePopulation() {
        List<AllocationSolution> population = new ArrayList<>();
        
        for (int i = 0; i < populationSize; i++) {
            AllocationSolution solution = new AllocationSolution(vms, hosts);
            
            // Alocar cada VM aleatoriamente em um host
            for (Vm vm : vms) {
                Host randomHost = hosts.get(random.nextInt(hosts.size()));
                solution.allocateVM(vm, randomHost);
            }
            
            population.add(solution);
        }
        
        return population;
    }
    
    /**
     * Avalia o fitness de uma solução focada em economia de energia
     * Penaliza: sobrecarga (forte), hosts ativos desnecessários (forte), 
     * desperdício de recursos (médio), comunicação (médio)
     */
    private double evaluateFitness(AllocationSolution solution) {
        double fitness = 0.0;
        int activeHostsCount = 0;
        
        // Para cada host
        for (Host host : hosts) {
            List<Vm> vmsOnHost = solution.getVmsOnHost(host);
            
            if (vmsOnHost.isEmpty()) {
                // Host ocioso não é penalizado (pois não está consumindo energia)
                continue;
            }
            
            activeHostsCount++;
            
            // Calcular demanda total de recursos
            double cpuDemand = 0.0;
            double ramDemand = 0.0;
            double storageDemand = 0.0;
            double bandwidthDemand = 0.0;
            
            for (Vm vm : vmsOnHost) {
                double vmMips = vm.getTotalMipsCapacity();
                cpuDemand += vmMips;
                ramDemand += vm.getRam().getCapacity();
                storageDemand += vm.getStorage().getCapacity();
                bandwidthDemand += vm.getBw().getCapacity();
            }
            
            // Capacidades do host
            double cpuCapacity = host.getTotalMipsCapacity();
            double ramCapacity = host.getRam().getCapacity();
            double storageCapacity = host.getStorage().getCapacity();
            double bandwidthCapacity = host.getBw().getCapacity();
            
            // Penalização por sobrecarga (FORTE - peso 10.0)
            if (cpuDemand > cpuCapacity) {
                fitness += 10.0 * (cpuDemand - cpuCapacity) / cpuCapacity;
            }
            if (ramDemand > ramCapacity) {
                fitness += 10.0 * (ramDemand - ramCapacity) / ramCapacity;
            }
            if (storageDemand > storageCapacity) {
                fitness += 10.0 * (storageDemand - storageCapacity) / storageCapacity;
            }
            if (bandwidthDemand > bandwidthCapacity) {
                fitness += 10.0 * (bandwidthDemand - bandwidthCapacity) / bandwidthCapacity;
            }
            
            // Calcular utilização média do host
            double cpuUtilization = cpuDemand / cpuCapacity;
            double ramUtilization = ramDemand / ramCapacity;
            double storageUtilization = storageDemand / storageCapacity;
            double bandwidthUtilization = bandwidthDemand / bandwidthCapacity;
            double avgUtilization = (cpuUtilization + ramUtilization + storageUtilization + bandwidthUtilization) / 4.0;
            
            // Penalização por desperdício de recursos quando a utilização é muito baixa (MÉDIO - peso 2.0)
            // Isso incentiva a consolidação: hosts com baixa utilização são fortemente penalizados
            if (avgUtilization < 0.3) {
                // Host com menos de 30% de utilização é penalizado
                // Quanto menor a utilização, maior a penalização
                double wastePenalty = 2.0 * (0.3 - avgUtilization) / 0.3;
                fitness += wastePenalty;
            } else {
                // Para hosts com utilização razoável, penalização menor pelo desperdício
                double cpuWaste = Math.max(0, cpuCapacity - cpuDemand) / cpuCapacity;
                double ramWaste = Math.max(0, ramCapacity - ramDemand) / ramCapacity;
                double storageWaste = Math.max(0, storageCapacity - storageDemand) / storageCapacity;
                double bandwidthWaste = Math.max(0, bandwidthCapacity - bandwidthDemand) / bandwidthCapacity;
                fitness += 0.5 * (cpuWaste + ramWaste + storageWaste + bandwidthWaste) / 4.0;
            }
        }
        
        // Penalização por número de hosts ativos (FORTE - peso 3.0 por host)
        // Quanto mais hosts ativos, mais energia consumida
        // Isso é o componente principal para economizar energia
        fitness += 3.0 * activeHostsCount;
        
        // Penalização por custo de comunicação (MÉDIO - peso 1.0)
        double communicationCost = calculateCommunicationCost(solution);
        fitness += 1.0 * communicationCost;
        
        return fitness;
    }
    
    /**
     * Calcula custo de comunicação quando VMs que se comunicam estão distantes
     */
    private double calculateCommunicationCost(AllocationSolution solution) {
        double cost = 0.0;
        
        for (int i = 0; i < vms.size(); i++) {
            for (int j = i + 1; j < vms.size(); j++) {
                Vm vm1 = vms.get(i);
                Vm vm2 = vms.get(j);
                
                double communication = communicationMatrix[i][j];
                if (communication > 0) {
                    Host host1 = solution.getHostForVM(vm1);
                    Host host2 = solution.getHostForVM(vm2);
                    
                    if (host1 != null && host2 != null) {
                        // Se estão em hosts diferentes, há custo de comunicação
                        if (!host1.equals(host2)) {
                            // Distância simplificada: número de hosts entre eles
                            int distance = Math.abs((int)(host1.getId() - host2.getId())) + 1;
                            cost += communication * distance;
                        }
                    }
                }
            }
        }
        
        return cost / (vms.size() * (vms.size() - 1) / 2.0); // Normalizar
    }
    
    /**
     * Seleção por torneio
     */
    private AllocationSolution tournamentSelection(List<AllocationSolution> population) {
        AllocationSolution best = null;
        double bestFitness = Double.MAX_VALUE;
        
        for (int i = 0; i < tournamentSize; i++) {
            AllocationSolution candidate = population.get(random.nextInt(population.size()));
            if (candidate.getFitness() < bestFitness) {
                bestFitness = candidate.getFitness();
                best = candidate;
            }
        }
        
        return new AllocationSolution(best);
    }
    
    /**
     * Crossover uniforme: para cada VM, escolhe aleatoriamente de qual pai herdar
     */
    private AllocationSolution uniformCrossover(AllocationSolution parent1, AllocationSolution parent2) {
        AllocationSolution child = new AllocationSolution(vms, hosts);
        
        for (Vm vm : vms) {
            if (random.nextDouble() < crossoverRate) {
                // Herda de parent1
                Host host = parent1.getHostForVM(vm);
                if (host != null) {
                    child.allocateVM(vm, host);
                }
            } else {
                // Herda de parent2
                Host host = parent2.getHostForVM(vm);
                if (host != null) {
                    child.allocateVM(vm, host);
                }
            }
        }
        
        return child;
    }
    
    /**
     * Mutação: move uma VM aleatória para outro host.
     * Prefere hosts já utilizados para incentivar consolidação e economia de energia.
     */
    private void mutate(AllocationSolution solution) {
        if (vms.isEmpty()) return;
        
        Vm selectedVM = vms.get(random.nextInt(vms.size()));
        
        // Filtrar hosts que podem acomodar a VM
        List<Host> availableHosts = new ArrayList<>();
        for (Host host : hosts) {
            if (canHostAccommodateVM(host, selectedVM, solution)) {
                availableHosts.add(host);
            }
        }
        
        if (availableHosts.isEmpty()) {
            // Nenhum host pode acomodar a VM, mutação cancelada
            return;
        }
        
        // Separar hosts já utilizados (com outras VMs) e hosts vazios
        List<Host> usedHosts = new ArrayList<>();
        List<Host> emptyHosts = new ArrayList<>();
        
        Host currentHost = solution.getHostForVM(selectedVM);
        
        for (Host host : availableHosts) {
            List<Vm> vmsOnHost = solution.getVmsOnHost(host);
            // Se o host já tem VMs (além da VM atual se ela estiver neste host), considera como usado
            int vmCountExcludingCurrent = vmsOnHost.size();
            if (host.equals(currentHost) && vmsOnHost.contains(selectedVM)) {
                vmCountExcludingCurrent--; // Não contar a VM atual
            }
            
            if (vmCountExcludingCurrent > 0) {
                usedHosts.add(host);
            } else {
                emptyHosts.add(host);
            }
        }
        
        Host newHost;
        // 70% de chance de preferir hosts já utilizados (consolidação)
        // Isso economiza energia ao evitar ligar hosts novos desnecessariamente
        if (!usedHosts.isEmpty() && random.nextDouble() < 0.7) {
            newHost = usedHosts.get(random.nextInt(usedHosts.size()));
        } else {
            // Escolhe aleatoriamente entre todos os hosts disponíveis
            newHost = availableHosts.get(random.nextInt(availableHosts.size()));
        }
        
        solution.reallocateVM(selectedVM, newHost);
    }
    
    /**
     * Verifica se um host pode acomodar uma VM sem sobrecarga
     */
    private boolean canHostAccommodateVM(Host host, Vm vm, AllocationSolution solution) {
        List<Vm> vmsOnHost = solution.getVmsOnHost(host);
        
        double cpuDemand = vm.getTotalMipsCapacity();
        double ramDemand = vm.getRam().getCapacity();
        double storageDemand = vm.getStorage().getCapacity();
        double bandwidthDemand = vm.getBw().getCapacity();
        
        for (Vm existingVM : vmsOnHost) {
            cpuDemand += existingVM.getTotalMipsCapacity();
            ramDemand += existingVM.getRam().getCapacity();
            storageDemand += existingVM.getStorage().getCapacity();
            bandwidthDemand += existingVM.getBw().getCapacity();
        }
        
        double cpuCapacity = host.getTotalMipsCapacity();
        double ramCapacity = host.getRam().getCapacity();
        double storageCapacity = host.getStorage().getCapacity();
        double bandwidthCapacity = host.getBw().getCapacity();
        
        return cpuDemand <= cpuCapacity &&
               ramDemand <= ramCapacity &&
               storageDemand <= storageCapacity &&
               bandwidthDemand <= bandwidthCapacity;
    }
    
    /**
     * Classe interna para representar uma solução de alocação
     */
    public static class AllocationSolution {
        private final Map<Vm, Host> vmToHost;
        private final Set<Host> activeHosts;
        private final List<Vm> vms;
        private final List<Host> hosts;
        private double fitness;
        
        public AllocationSolution(List<Vm> vms, List<Host> hosts) {
            this.vms = new ArrayList<>(vms);
            this.hosts = new ArrayList<>(hosts);
            this.vmToHost = new HashMap<>();
            this.activeHosts = new HashSet<>();
            this.fitness = Double.MAX_VALUE;
        }
        
        public AllocationSolution(AllocationSolution other) {
            this.vms = new ArrayList<>(other.vms);
            this.hosts = new ArrayList<>(other.hosts);
            this.vmToHost = new HashMap<>(other.vmToHost);
            this.activeHosts = new HashSet<>(other.activeHosts);
            this.fitness = other.fitness;
        }
        
        public void allocateVM(Vm vm, Host host) {
            vmToHost.put(vm, host);
            activeHosts.add(host);
        }
        
        public void reallocateVM(Vm vm, Host newHost) {
            Host oldHost = vmToHost.remove(vm);
            if (oldHost != null) {
                // Verificar se host antigo ainda tem VMs
                boolean hostStillUsed = vmToHost.values().contains(oldHost);
                if (!hostStillUsed) {
                    activeHosts.remove(oldHost);
                }
            }
            vmToHost.put(vm, newHost);
            activeHosts.add(newHost);
        }
        
        public Host getHostForVM(Vm vm) {
            return vmToHost.get(vm);
        }
        
        public List<Vm> getVmsOnHost(Host host) {
            List<Vm> result = new ArrayList<>();
            for (Map.Entry<Vm, Host> entry : vmToHost.entrySet()) {
                if (entry.getValue().equals(host)) {
                    result.add(entry.getKey());
                }
            }
            return result;
        }
        
        public int getActiveHostsCount() {
            return activeHosts.size();
        }
        
        public Map<Vm, Host> getVmToHost() {
            return new HashMap<>(vmToHost);
        }
        
        public double getFitness() {
            return fitness;
        }
        
        public void setFitness(double fitness) {
            this.fitness = fitness;
        }
    }
}

