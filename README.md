# Genetic Algorithm for VM Allocation

Este projeto é para a matéria de **Computação Evolucionária** no **IC UFAL**. O objetivo é utilizar algoritmo genético para alocação de máquinas virtuais com o objetivo de diminuir o consumo de energia.

## Equipe

- **Ramon Barros**
- **Afranio Yago**
- **Victor Alexandre**

## Objetivo

Implementar um algoritmo genético para otimizar a alocação de máquinas virtuais (VMs) em hosts físicos de datacenters, focando na redução do consumo de energia através da consolidação de VMs em menos servidores ativos.

## Requisitos

- Java 17+
- Maven 3.6+

## Como Rodar

### Compilar o projeto

```bash
mvn clean compile
```

### Executar a simulação

```bash
mvn exec:java -Dexec.mainClass="geneticovm.genetic.CloudSimSimulation"
```

## Estrutura do Projeto

```
src/main/java/geneticovm/genetic/
├── EnergyAwareGeneticAlgorithm.java  (Implementação do Algoritmo Genético)
└── CloudSimSimulation.java          (Simulação CloudSim com o algoritmo)
```

## Algoritmo Genético

O algoritmo implementa as seguintes etapas:

1. **Inicialização da População**: Gera uma população aleatória de soluções de alocação
2. **Avaliação (Fitness)**: Penaliza sobrecarga (forte), desperdício de recursos (médio) e custo de comunicação (médio)
3. **Seleção por Torneio**: Seleciona os melhores indivíduos para reprodução
4. **Crossover Uniforme**: Combina soluções de dois pais para criar filhos
5. **Mutação**: Move VMs aleatoriamente para outros hosts (respeitando restrições)
6. **Condição de Término**: 20 gerações (conforme especificado)

### Parâmetros do Algoritmo

- **População**: 50 indivíduos
- **Gerações**: 20
- **Taxa de Crossover**: 0.8
- **Taxa de Mutação**: 0.1
- **Tamanho do Torneio**: 3

### Função de Fitness

A função de fitness penaliza:
- **Sobrecarga** (peso 10.0): Quando a demanda de recursos excede a capacidade do host
- **Desperdício de recursos** (peso 1.0): Recursos não utilizados em hosts ativos
- **Custo de comunicação** (peso 1.0): VMs que se comunicam estão distantes na rede

## Resultados

O algoritmo busca soluções que:
- Minimizam o número de hosts ativos (economia de energia)
- Reduzem desperdício de recursos
- Minimizam custos de comunicação
- Evitam sobrecarga de recursos

## Tecnologias

- **CloudSim Plus 8.5.5**: Framework de simulação de cloud computing
- **Java 17**: Linguagem de programação
- **Maven**: Gerenciamento de dependências
