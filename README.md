# Simulador do Algoritmo de Tomasulo
O intuito desse trabalho é simular o funcionamento do algoritmo de Tomasulo.

Segue abaixo os tipos de instrução suportadas e suas respectivas durações em termos de ciclos em tempo de execução
ADD: 1 ciclos
ADDI: 1 ciclos
SUB: 1 ciclos
SUBI: 1 ciclos
BEQ: 2 ciclos
MUL: 3 ciclos
MULI: 3 ciclos
DIV: 3 ciclos
DIVI: 3 ciclos
LOAD: 5 ciclos
STORE: 5 ciclos


Em termos de estrutura o projeto conta com:
-> 16 registradores arquiteturais e 32 registradores físicos
-> 8 slots de buffer de reordenamento
-> 7 estações de reserva (3 para ADD/SUB, 2 para MUL/DIV, 2 LOAD/STORE)

para compilar: javac -d bin src/simulador/*.java src/gui/*.java
para executar: java -cp bin gui.SimuladorMain

As métricas de desempenho utilizadas são:
->IPC: Instruções executadas/total de ciclos
->Ciclos de bolha: ciclos ociosos no pipeline
