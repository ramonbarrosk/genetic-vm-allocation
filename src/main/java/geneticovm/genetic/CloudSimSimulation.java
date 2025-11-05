package geneticovm.genetic;

import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;
import org.cloudsimplus.utilizationmodels.UtilizationModelFull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CloudSimSimulation {
    
    public static void main(String[] args) {
        System.out.println("=== Simulação CloudSim com Algoritmo Genético ===\n");
        
        System.setProperty("org.slf4j.simpleLogger.log.org.cloudsimplus", "warn");
        
        CloudSimPlus cloudsim = new CloudSimPlus();
        
        List<Host> hosts = createHosts();
        System.out.println("Criados " + hosts.size() + " servidores (hosts)");
        
        Datacenter datacenter = createDatacenter(cloudsim, hosts);
        System.out.println("Datacenter criado com " + hosts.size() + " hosts\n");
        
        List<Vm> vms = createVMs();
        System.out.println("Criadas " + vms.size() + " máquinas virtuais (VMs)");
        
        List<Cloudlet> cloudlets = createCloudlets();
        System.out.println("Criadas " + cloudlets.size() + " tarefas (Cloudlets)\n");
        
        System.out.println("=== ALGORITMO GENÉTICO PARA ALOCAÇÃO DE VMs ===\n");
        EnergyAwareGeneticAlgorithm ga = new EnergyAwareGeneticAlgorithm(
            vms, hosts,
            50,
            20,
            0.8,
            0.1,
            3,
            System.currentTimeMillis()
        );
        
        EnergyAwareGeneticAlgorithm.AllocationSolution bestSolution = ga.run();
        
        DatacenterBroker broker = new DatacenterBrokerSimple(cloudsim);
        broker.submitVmList(vms);
        broker.submitCloudletList(cloudlets);
        
        applyGeneticAllocation(broker, bestSolution, vms, hosts);
        System.out.println("Alocação genética aplicada ao broker\n");
        
        System.out.println("Executando simulação...");
        cloudsim.start();
        
        showResults(broker, cloudlets, vms, bestSolution, hosts);
        
        System.out.println("\nSimulação concluída!");
    }
    
    private static List<Host> createHosts() {
        List<Host> hosts = new ArrayList<>();
        int[][] hostConfigs = {
            {4, 1000, 16384, 10000, 1000000},
            {2, 2000, 8192, 5000, 500000},
            {8, 1500, 32768, 20000, 2000000},
            {4, 1200, 16384, 10000, 1000000},
            {6, 1500, 24576, 15000, 1500000},
            {2, 1800, 8192, 5000, 500000},
            {8, 1400, 32768, 20000, 2000000},
            {4, 1600, 16384, 10000, 1000000},
            {6, 1300, 24576, 15000, 1500000},
            {4, 1100, 16384, 10000, 1000000}
        };
        
        for (int i = 0; i < hostConfigs.length; i++) {
            int[] config = hostConfigs[i];
            int cpuCount = config[0];
            int mips = config[1];
            int ram = config[2];
            int bw = config[3];
            int storage = config[4];
            
            List<Pe> pes = new ArrayList<>();
            for (int j = 0; j < cpuCount; j++) {
                pes.add(new PeSimple(mips));
            }
            
            Host host = new HostSimple(ram, bw, storage, pes);
            host.setId(i);
            hosts.add(host);
        }
        
        return hosts;
    }
    
    private static Datacenter createDatacenter(CloudSimPlus cloudsim, List<Host> hosts) {
        return new DatacenterSimple(cloudsim, hosts);
    }
    
    private static List<Vm> createVMs() {
        List<Vm> vms = new ArrayList<>();
        int[][] vmConfigs = {
            {1000, 1, 2048, 1000, 100000},
            {1000, 1, 2048, 1000, 100000},
            {1500, 2, 4096, 2000, 200000},
            {1000, 1, 1024, 1000, 50000},
            {1200, 1, 2048, 1500, 150000},
            {1000, 1, 2048, 1000, 100000},
            {1500, 2, 4096, 2000, 200000},
            {1000, 1, 1024, 1000, 50000},
            {1800, 2, 4096, 2000, 200000},
            {1000, 1, 2048, 1000, 100000},
            {1200, 1, 3072, 1500, 150000},
            {1000, 1, 1024, 1000, 50000},
            {1500, 2, 4096, 2000, 200000},
            {1000, 1, 2048, 1000, 100000},
            {1600, 2, 4096, 2000, 200000},
            {1000, 1, 1024, 1000, 50000},
            {1200, 1, 2048, 1500, 150000},
            {1000, 1, 2048, 1000, 100000},
            {1500, 2, 4096, 2000, 200000},
            {1000, 1, 1024, 1000, 50000}
        };
        
        for (int i = 0; i < vmConfigs.length; i++) {
            int[] config = vmConfigs[i];
            int mips = config[0];
            int cpus = config[1];
            int ram = config[2];
            int bw = config[3];
            int storage = config[4];
            
            Vm vm = new VmSimple(mips, cpus);
            vm.setRam(ram).setBw(bw).setSize(storage);
            vm.setId(i);
            vms.add(vm);
        }
        
        return vms;
    }
    
    private static List<Cloudlet> createCloudlets() {
        List<Cloudlet> cloudlets = new ArrayList<>();
        int[] cloudletSizes = {
            10000, 50000, 100000, 20000, 75000,
            15000, 60000, 120000, 30000, 80000,
            25000, 70000, 110000, 40000, 90000,
            18000, 55000, 95000, 35000, 65000
        };
        
        for (int i = 0; i < cloudletSizes.length; i++) {
            Cloudlet cloudlet = new CloudletSimple(cloudletSizes[i], 1);
            cloudlet.setUtilizationModel(new UtilizationModelFull());
            cloudlet.setId(i);
            cloudlets.add(cloudlet);
        }
        
        return cloudlets;
    }
    
    private static void applyGeneticAllocation(DatacenterBroker broker, 
                                             EnergyAwareGeneticAlgorithm.AllocationSolution solution,
                                             List<Vm> vms, List<Host> hosts) {
        System.out.println("Alocação do Algoritmo Genético:");
        System.out.println("-".repeat(60));
        var vmToHost = solution.getVmToHost();
        for (Map.Entry<Vm, Host> entry : vmToHost.entrySet()) {
            System.out.printf("  VM %d → Host %d%n", 
                (int)entry.getKey().getId(), (int)entry.getValue().getId());
        }
        System.out.printf("  Hosts ativos: %d de %d%n", solution.getActiveHostsCount(), hosts.size());
        System.out.printf("  Fitness: %.4f%n", solution.getFitness());
        System.out.println();
    }
    
    private static void showResults(DatacenterBroker broker, List<Cloudlet> cloudlets, List<Vm> vms,
                                   EnergyAwareGeneticAlgorithm.AllocationSolution gaSolution,
                                   List<Host> hosts) {
        System.out.println("\n=== RESULTADOS DA SIMULAÇÃO ===\n");
        
        System.out.println("INFORMAÇÕES DAS VMs:");
        System.out.println("-".repeat(60));
        for (Vm vm : broker.getVmCreatedList()) {
            try {
                var host = vm.getHost();
                if (host != null) {
                    System.out.printf("VM %d: CRIADA E ALOCADA%n", vm.getId());
                    System.out.printf("  - Alocada em Host: %d%n", host.getId());
                    System.out.printf("  - CPUs: %d, RAM: %d MB, Storage: %d MB%n", 
                        vm.getPesNumber(), vm.getRam().getCapacity(), vm.getStorage().getCapacity());
                } else {
                    System.out.printf("VM %d: NÃO ALOCADA%n", vm.getId());
                    System.out.printf("  - Esta VM não pôde ser alocada em nenhum host%n");
                }
            } catch (Exception e) {
                System.out.printf("VM %d: ERRO AO VERIFICAR%n", vm.getId());
            }
        }
        System.out.println();
        
        System.out.println("RESULTADOS DOS CLOUDLETS:");
        System.out.println("-".repeat(60));
        for (Cloudlet cloudlet : broker.getCloudletFinishedList()) {
            System.out.printf("Cloudlet %d:%n", cloudlet.getId());
            System.out.printf("  - Status: %s%n", cloudlet.getStatus());
            if (cloudlet.getVm() != null) {
                System.out.printf("  - Executado em VM: %d%n", cloudlet.getVm().getId());
            }
            System.out.printf("  - Tempo de início: %.2f segundos%n", cloudlet.getStartTime());
            System.out.printf("  - Tempo de conclusão: %.2f segundos%n", cloudlet.getFinishTime());
            double execTime = cloudlet.getFinishTime() - cloudlet.getStartTime();
            System.out.printf("  - Tempo de execução: %.2f segundos%n", execTime);
            System.out.printf("  - Comprimento: %d MI (Million Instructions)%n", cloudlet.getLength());
            System.out.println();
        }
        
        System.out.println("ESTATÍSTICAS GERAIS:");
        System.out.println("-".repeat(60));
        double totalExecutionTime = broker.getCloudletFinishedList().stream()
            .mapToDouble(Cloudlet::getFinishTime)
            .max()
            .orElse(0.0);
        System.out.printf("Tempo total de simulação: %.2f segundos%n", totalExecutionTime);
        System.out.printf("VMs criadas: %d%n", broker.getVmCreatedList().size());
        System.out.printf("Cloudlets executados: %d%n", broker.getCloudletFinishedList().size());
        
        if (gaSolution != null) {
            System.out.println("\nESTATÍSTICAS DO ALGORITMO GENÉTICO:");
            System.out.println("-".repeat(60));
            System.out.printf("Hosts ativos utilizados: %d de %d%n", 
                gaSolution.getActiveHostsCount(), hosts.size());
            System.out.printf("Fitness final: %.4f%n", gaSolution.getFitness());
            System.out.printf("Eficiência energética: %.2f%% (menos hosts = mais eficiente)%n",
                (1.0 - (double)gaSolution.getActiveHostsCount() / hosts.size()) * 100);
        }
    }
}

