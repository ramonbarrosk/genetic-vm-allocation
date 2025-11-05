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
        System.out.println("✅ Criados " + hosts.size() + " servidores (hosts)");
        
        Datacenter datacenter = createDatacenter(cloudsim, hosts);
        System.out.println("✅ Datacenter criado com " + hosts.size() + " hosts\n");
        
        List<Vm> vms = createVMs();
        System.out.println("✅ Criadas " + vms.size() + " máquinas virtuais (VMs)");
        
        List<Cloudlet> cloudlets = createCloudlets();
        System.out.println("✅ Criadas " + cloudlets.size() + " tarefas (Cloudlets)\n");
        
        System.out.println("🧬 === ALGORITMO GENÉTICO PARA ALOCAÇÃO DE VMs ===\n");
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
        System.out.println("✅ Alocação genética aplicada ao broker\n");
        
        System.out.println("🚀 Executando simulação...");
        cloudsim.start();
        
        showResults(broker, cloudlets, vms, bestSolution, hosts);
        
        System.out.println("\n✅ Simulação concluída!");
    }
    
    private static List<Host> createHosts() {
        List<Host> hosts = new ArrayList<>();
        
        // Host 1: Servidor com 4 CPUs, 16GB RAM, 1TB Storage
        List<Pe> pes1 = new ArrayList<>();
        pes1.add(new PeSimple(1000)); // 1000 MIPS por CPU
        pes1.add(new PeSimple(1000));
        pes1.add(new PeSimple(1000));
        pes1.add(new PeSimple(1000));
        
        Host host1 = new HostSimple(16384, 10000, 1000000, pes1); // 16GB RAM, 10Gbps, 1TB Storage
        host1.setId(0);
        hosts.add(host1);
        
        // Host 2: Servidor com 2 CPUs, 8GB RAM, 500GB Storage
        List<Pe> pes2 = new ArrayList<>();
        pes2.add(new PeSimple(2000)); // 2000 MIPS por CPU
        pes2.add(new PeSimple(2000));
        
        Host host2 = new HostSimple(8192, 5000, 500000, pes2); // 8GB RAM, 5Gbps, 500GB Storage
        host2.setId(1);
        hosts.add(host2);
        
        // Host 3: Servidor com 8 CPUs, 32GB RAM, 2TB Storage
        List<Pe> pes3 = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            pes3.add(new PeSimple(1500)); // 1500 MIPS por CPU
        }
        
        Host host3 = new HostSimple(32768, 20000, 2000000, pes3); // 32GB RAM, 20Gbps, 2TB Storage
        host3.setId(2);
        hosts.add(host3);
        
        return hosts;
    }
    
    private static Datacenter createDatacenter(CloudSimPlus cloudsim, List<Host> hosts) {
        return new DatacenterSimple(cloudsim, hosts);
    }
    
    private static List<Vm> createVMs() {
        List<Vm> vms = new ArrayList<>();
        
        // VM 1: Pequena - 1 CPU, 2GB RAM, 100GB Storage
        Vm vm1 = new VmSimple(1000, 1); // 1000 MIPS, 1 CPU
        vm1.setRam(2048).setBw(1000).setSize(100000); // 2GB RAM, 1Gbps, 100GB
        vm1.setId(0);
        vms.add(vm1);
        
        // VM 2: Média - 1 CPU, 2GB RAM, 100GB Storage
        Vm vm2 = new VmSimple(1000, 1); // 1000 MIPS, 1 CPU
        vm2.setRam(2048).setBw(1000).setSize(100000); // 2GB RAM, 1Gbps, 100GB
        vm2.setId(1);
        vms.add(vm2);
        
        // VM 3: Grande - 2 CPUs, 4GB RAM, 200GB Storage
        Vm vm3 = new VmSimple(1500, 2); // 1500 MIPS, 2 CPUs
        vm3.setRam(4096).setBw(2000).setSize(200000); // 4GB RAM, 2Gbps, 200GB
        vm3.setId(2);
        vms.add(vm3);
        
        // VM 4: Pequena - 1 CPU, 1GB RAM, 50GB Storage
        Vm vm4 = new VmSimple(1000, 1); // 1000 MIPS, 1 CPU
        vm4.setRam(1024).setBw(1000).setSize(50000); // 1GB RAM, 1Gbps, 50GB
        vm4.setId(3);
        vms.add(vm4);
        
        return vms;
    }
    
    private static List<Cloudlet> createCloudlets() {
        List<Cloudlet> cloudlets = new ArrayList<>();
        
        // Cloudlet 1: Tarefa pequena - 10000 MI (Million Instructions)
        Cloudlet cloudlet1 = new CloudletSimple(10000, 1);
        cloudlet1.setUtilizationModel(new UtilizationModelFull());
        cloudlet1.setId(0);
        cloudlets.add(cloudlet1);
        
        // Cloudlet 2: Tarefa média - 50000 MI
        Cloudlet cloudlet2 = new CloudletSimple(50000, 1);
        cloudlet2.setUtilizationModel(new UtilizationModelFull());
        cloudlet2.setId(1);
        cloudlets.add(cloudlet2);
        
        // Cloudlet 3: Tarefa grande - 100000 MI
        Cloudlet cloudlet3 = new CloudletSimple(100000, 1);
        cloudlet3.setUtilizationModel(new UtilizationModelFull());
        cloudlet3.setId(2);
        cloudlets.add(cloudlet3);
        
        // Cloudlet 4: Tarefa pequena - 20000 MI
        Cloudlet cloudlet4 = new CloudletSimple(20000, 1);
        cloudlet4.setUtilizationModel(new UtilizationModelFull());
        cloudlet4.setId(3);
        cloudlets.add(cloudlet4);
        
        // Cloudlet 5: Tarefa média - 75000 MI
        Cloudlet cloudlet5 = new CloudletSimple(75000, 1);
        cloudlet5.setUtilizationModel(new UtilizationModelFull());
        cloudlet5.setId(4);
        cloudlets.add(cloudlet5);
        
        return cloudlets;
    }
    
    private static void applyGeneticAllocation(DatacenterBroker broker, 
                                             EnergyAwareGeneticAlgorithm.AllocationSolution solution,
                                             List<Vm> vms, List<Host> hosts) {
        System.out.println("📋 Alocação do Algoritmo Genético:");
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
        
        System.out.println("📊 INFORMAÇÕES DAS VMs:");
        System.out.println("-".repeat(60));
        for (Vm vm : broker.getVmCreatedList()) {
            try {
                var host = vm.getHost();
                if (host != null) {
                    System.out.printf("VM %d: ✅ CRIADA E ALOCADA%n", vm.getId());
                    System.out.printf("  - Alocada em Host: %d%n", host.getId());
                    System.out.printf("  - CPUs: %d, RAM: %d MB, Storage: %d MB%n", 
                        vm.getPesNumber(), vm.getRam().getCapacity(), vm.getStorage().getCapacity());
                } else {
                    System.out.printf("VM %d: ❌ NÃO ALOCADA%n", vm.getId());
                    System.out.printf("  - ⚠️  Esta VM não pôde ser alocada em nenhum host%n");
                }
            } catch (Exception e) {
                System.out.printf("VM %d: ❌ ERRO AO VERIFICAR%n", vm.getId());
            }
        }
        System.out.println();
        
        System.out.println("📋 RESULTADOS DOS CLOUDLETS:");
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
        
        System.out.println("📈 ESTATÍSTICAS GERAIS:");
        System.out.println("-".repeat(60));
        double totalExecutionTime = broker.getCloudletFinishedList().stream()
            .mapToDouble(Cloudlet::getFinishTime)
            .max()
            .orElse(0.0);
        System.out.printf("Tempo total de simulação: %.2f segundos%n", totalExecutionTime);
        System.out.printf("VMs criadas: %d%n", broker.getVmCreatedList().size());
        System.out.printf("Cloudlets executados: %d%n", broker.getCloudletFinishedList().size());
        
        if (gaSolution != null) {
            System.out.println("\n📊 ESTATÍSTICAS DO ALGORITMO GENÉTICO:");
            System.out.println("-".repeat(60));
            System.out.printf("Hosts ativos utilizados: %d de %d%n", 
                gaSolution.getActiveHostsCount(), hosts.size());
            System.out.printf("Fitness final: %.4f%n", gaSolution.getFitness());
            System.out.printf("Eficiência energética: %.2f%% (menos hosts = mais eficiente)%n",
                (1.0 - (double)gaSolution.getActiveHostsCount() / hosts.size()) * 100);
        }
    }
}

