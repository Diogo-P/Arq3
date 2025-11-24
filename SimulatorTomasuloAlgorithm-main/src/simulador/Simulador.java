package simulador;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class Simulador {
    private static final int LIMITE_CICLOS = 100; // limite de ciclos para detectar loop
    private static final int LIMITE_INSTRUCAO = 50; // limite de ciclos para uma instrucao

    // Configuracoes do simulador
    private static final int TAMANHO_ROB = 8;
    private static final int NUM_ESTACOES_ADD = 3;
    private static final int NUM_ESTACOES_MUL = 3;
    private static final int NUM_ESTACOES_LOAD = 3;
    private static final int NUM_ESTACOES_BRANCHES = 3;
    private static final int NUM_REGISTRADORES_PRIVADOS = 32;
    private static final int NUM_REGISTRADORES_PUBLICOS = 16;

    // Estruturas de dados principais
    private Map<String, Float> bancoRegistradores; // R1 -> valor
    private Map<String, Float> bancoPrivado; // P1 -> valor
    private Map<String, String> mapaRenomeacao; // R1 -> P5
    private Queue<String> filaRegistradoresLivres; // Registradores privados livres

    private List<ReorderBufferSlot> rob; // Buffer de Reordenamento
    private int robHead; // Cabeça do ROB (proximo a fazer commit)
    private int robTail; // Cauda do ROB (proximo slot livre)

    private List<EstacaoDeReserva> estacoesAdd; // Estacoes para ADD/SUB
    private List<EstacaoDeReserva> estacoesMul; // Estacoes para MUL/DIV
    private List<EstacaoDeReserva> estacoesLoad; // Estacoes para LOAD/STORE
    private List<EstacaoDeReserva> estacoesBranches; // Estacoes para branches

    private Map<Integer, Float> memoria;

    // Estado do simulador
    private List<Instrucao> instrucoes;
    private int pc; // Program Counter
    private int cicloAtual;
    private int totalCiclos;
    private int ciclosBolha;
    private boolean simulacaoCompleta;

    // Estatisticas
    private int instrucoesExecutadas;
    private List<String> logExecucao;

    /**
     * Construtor do simulador
     */
    public Simulador() {
//passo 3
        inicializarSimulador();
    }

    /*
     * Metodo de inicializacao do simulador
     * Configura os registradores, buffer de reordenamento, estacoes de reserva e
     * memoria.
     */
    private void inicializarSimulador() {
//passo 4
        // Inicializando Banco de Registradores
        bancoRegistradores = new HashMap<>();
// passo 5
        for (int i = 0; i <= NUM_REGISTRADORES_PUBLICOS; i++) {
            bancoRegistradores.put("R" + i, (float) i);
        }
//passo 6
        // Inicializando Banco de Registradores privados
        bancoPrivado = new HashMap<>();
//passo 7
        filaRegistradoresLivres = new LinkedList<>();
//passo 8
        for (int i = 0; i <= NUM_REGISTRADORES_PRIVADOS; i++) {
            bancoPrivado.put("P" + i, (float) i);
            filaRegistradoresLivres.offer("P" + i);
        }
//passo 9
        mapaRenomeacao = new HashMap<>();
//passo 10
        // Inicializando Buffer de Reordenamento
        rob = new ArrayList<>();
//passo 11
        for (int i = 0; i < TAMANHO_ROB; i++) {
            rob.add(new ReorderBufferSlot(i));
        }
//passo 12
        robHead = 0;
        robTail = 0;

        // Inicializando Estacoes de Reserva
        estacoesAdd = new ArrayList<>();
//passo 13
        for (int i = 0; i < NUM_ESTACOES_ADD; i++) {
            estacoesAdd.add(new EstacaoDeReserva("Add" + (i + 1)));
        }
//passo 14
        estacoesMul = new ArrayList<>();
        for (int i = 0; i < NUM_ESTACOES_MUL; i++) {
            estacoesMul.add(new EstacaoDeReserva("Mult" + (i + 1)));
        }
//passo 15
        estacoesLoad = new ArrayList<>();
        for (int i = 0; i < NUM_ESTACOES_LOAD; i++) {
            estacoesLoad.add(new EstacaoDeReserva("Load" + (i + 1)));
        }
//passo 16
        estacoesBranches = new ArrayList<>();
        for (int i = 0; i < NUM_ESTACOES_BRANCHES; i++) {
            estacoesBranches.add(new EstacaoDeReserva("Branch" + (i + 1)));
        }
//passo 17
        // Iniciando memoria
        memoria = new HashMap<>();
        for (int i = 0; i < 1024; i++) {
            memoria.put(i, (float) i); // Inicializando memoria com zeros
        }
//passo 18
        // Inicializando estado do simulador
        instrucoes = new ArrayList<>();
        pc = 0;
        cicloAtual = 0;
        totalCiclos = 0;
        ciclosBolha = 0;
        simulacaoCompleta = false;
        instrucoesExecutadas = 0;
        logExecucao = new ArrayList<>();
    }

    public void reiniciar() {

        for (int i = 0; i < instrucoes.size(); i++) {
            instrucoes.get(i).setEstadoExecucao(0);
            instrucoes.get(i).resetExecucoes();
        }

        // Inicializando Banco de Registradores
        bancoRegistradores = new HashMap<>();
        for (int i = 0; i <= NUM_REGISTRADORES_PUBLICOS; i++) {
            bancoRegistradores.put("R" + i, (float) i);
        }

        // Inicializando Banco de Registradores privados
        bancoPrivado = new HashMap<>();
        filaRegistradoresLivres = new LinkedList<>();
        for (int i = 0; i <= NUM_REGISTRADORES_PRIVADOS; i++) {
            bancoPrivado.put("P" + i, (float) i);
            filaRegistradoresLivres.offer("P" + i);
        }

        mapaRenomeacao = new HashMap<>();

        // Inicializando Buffer de Reordenamento
        rob = new ArrayList<>(TAMANHO_ROB);
        for (int i = 0; i < TAMANHO_ROB; i++) {
            rob.add(new ReorderBufferSlot(i));
        }
        robHead = 0;
        robTail = 0;

        // Inicializando Estacoes de Reserva
        estacoesAdd = new ArrayList<>();
        for (int i = 0; i < NUM_ESTACOES_ADD; i++) {
            estacoesAdd.add(new EstacaoDeReserva("Add" + (i + 1)));
        }

        estacoesMul = new ArrayList<>();
        for (int i = 0; i < NUM_ESTACOES_MUL; i++) {
            estacoesMul.add(new EstacaoDeReserva("Mult" + (i + 1)));
        }

        estacoesLoad = new ArrayList<>();
        for (int i = 0; i < NUM_ESTACOES_LOAD; i++) {
            estacoesLoad.add(new EstacaoDeReserva("Load" + (i + 1)));
        }

        estacoesBranches = new ArrayList<>();
        for (int i = 0; i < NUM_ESTACOES_BRANCHES; i++) {
            estacoesBranches.add(new EstacaoDeReserva("Branch" + (i + 1)));
        }

        // Iniciando memoria
        memoria = new HashMap<>();
        for (int i = 0; i < 1024; i++) {
            memoria.put(i, (float) i); // Inicializando memoria com zeros
        }

        // Inicializando estado do simulador
        pc = 0;
        cicloAtual = 0;
        totalCiclos = 0;
        ciclosBolha = 0;
        simulacaoCompleta = false;
        instrucoesExecutadas = 0;
        logExecucao = new ArrayList<>();
    }

    public void proximoCiclo() {
        //System.out.println("Executando ciclo: " + cicloAtual);
        if (!simulacaoCompleta) {
            logExecucao.add("Ciclo " + (cicloAtual+1));

            // --- LOOP DETECTION ---
            // Verifica se o numero de ciclos excedeu o limite
            if (cicloAtual > LIMITE_CICLOS) {
                logExecucao.add("Timeout: Numero de ciclos excedeu o limite de " + LIMITE_CICLOS + ". Simulacao interrompida.");
                simulacaoCompleta = true;
                logExecucao.add("Simulacao completa. Total de ciclos gastos: 0 (loop detectado)");
                return;
            }
            // Verifica se alguma instrucao esta "presa" por muitos ciclos
            for (Instrucao inst : instrucoes) {
                if (inst.getEstadoExecucao() > 0 && inst.getQtdeExecucoes() > LIMITE_INSTRUCAO) {
                    logExecucao.add("Timeout: Instrucao presa por mais de " + LIMITE_INSTRUCAO + " ciclos: " + inst.toString());
                    simulacaoCompleta = true;
                    logExecucao.add("Simulacao completa. Total de ciclos gastos: 0 (loop detectado)");
                    return;
                }
            }
            // --- FIM LOOP DETECTION ---

            writeResult();

            execute();

            issue();

            commit();
            if (pc == instrucoes.size() && robVazio()) {
                simulacaoCompleta = true;
                logExecucao.add("Simulacao completa. Total de ciclos gastos: " + totalCiclos);
                totalCiclos = cicloAtual - 1;
            } else {
                logExecucao.add("-------------------------------------------------------------");
            }
            cicloAtual++;
            confereSituacaoROB();
        }
    }

    public void confereSituacaoROB() {
        for (int i = robHead; i != robTail; i = (i + 1) % TAMANHO_ROB) {
            ReorderBufferSlot slot = rob.get(i);
            //System.out.println("Slot " + i + ": " + slot.getInstrucao() + ", Busy: " + slot.isBusy());
        }
    }

    void writeResult() {
        // Verifica estacoes de reserva que terminaram a execucao
        List<EstacaoDeReserva> todasEstacoes = new ArrayList<>();
        todasEstacoes.addAll(estacoesAdd);
        todasEstacoes.addAll(estacoesMul);
        todasEstacoes.addAll(estacoesLoad);
        todasEstacoes.addAll(estacoesBranches);

        for (EstacaoDeReserva estacao : todasEstacoes) {
            // System.out.println(
            // "Ciclos restantes para a estacao " + estacao.getNome() + ": " +
            // estacao.getCiclosRestantes());
            if (estacao.isBusy() && estacao.getCiclosRestantes() == 0) {
                String regPrivado = estacao.getDest();

                // Atualiza o slot do ROB
                ReorderBufferSlot slot = encontrarSlotROB(regPrivado);
                if (slot.getCicloEscrita() != cicloAtual) {
                    Float resultado = estacao.calcularResultado();
                    slot.setCicloEscrita(cicloAtual);
                    slot.setCicloCommit(cicloAtual);
                    slot.setEstado(EstadoInstrucao.ESCRITA);
                    slot.setPronto(true);
                    Instrucao inst = slot.getInstrucao();
                    if (inst != null) inst.setEstadoExecucao(3); // resultado escrito
                    if (estacao.getOp().isMemoryOperation()) {
                        // Para LOAD, lê da memoria
                        if (estacao.getOp() == OpCode.LOAD) {
                            int endereco = resultado.intValue();
                            resultado = memoria.getOrDefault(endereco, 0.0f);

                            // Propaga resultado via CDB para estacoes de reserva que estavam esperando
                            propagarResultadoCDB(regPrivado, resultado);
                        } else { // STORE
                            int endereco = resultado.intValue();
                            Float valor = bancoPrivado.get(regPrivado);
                            memoria.put(endereco, valor);
                            resultado = valor; // Para STORE, o resultado e o valor armazenado
                        }
                    } else if (estacao.getOp().isBranch()) {
                        //System.out.println("Branch detected");
                        if (resultado == 1) {
                            executarBEQ(slot);
                        }
                    } else {
                        // Propaga resultado via CDB para estacoes de reserva que estavam esperando
                        propagarResultadoCDB(regPrivado, resultado);
                    }

                    slot.marcarResultadoPronto(resultado, cicloAtual);
                    // logExecucao.add("Write Result: " + estacao.getNome() + " -> ROB" + regPrivado + " = " + resultado);
                    estacao.limpar();
                }

            }
        }
    }

    private void executarBEQ(ReorderBufferSlot slot) {
        for (int i = robHead; i != robTail; i = (i + 1) % TAMANHO_ROB) {
            if (!rob.get(i).equals(slot)) {
                if (rob.get(i).isBusy()) {
                    if (rob.get(i).getCicloIssue() != -1 && rob.get(i).getCicloIssue() >= slot.getCicloIssue()) {
                        Instrucao inst = rob.get(i).getInstrucao();
                        if (inst != null) {
                            logExecucao.add("BEQ executado, instrucao cancelada: " + inst.toString());
                        }
                        rob.get(i).limpar();
                    }
                }
            }
        }
        pc = slot.getInstrucao().getImediato() - 1; // Atualiza o PC para o endereço do branch
    }

    /*
     * Funcao que encontra um slot do ROB baseado no registrador renomeado.
     * Se o registrador renomeado for encontrado, retorna o slot correspondente.
     * Caso contrario, retorna null.
     */
    ReorderBufferSlot encontrarSlotROB(String regPrivado) {
        ReorderBufferSlot slotEncontrado = null;
        for (ReorderBufferSlot slot : rob) {
            if (slot.isBusy() && slot.getRegistradorRenomeado().equals(regPrivado)) {
                slotEncontrado = slot;
            }
        }
        return slotEncontrado;
    }

    /**
     * Propaga resultado via Common Data Bus (CDB)
     */
    private void propagarResultadoCDB(String regPrivado, Float valor) {
        // Atualiza estacoes de reserva que estavam esperando este resultado
        List<EstacaoDeReserva> todasEstacoes = new ArrayList<>();
        todasEstacoes.addAll(estacoesAdd);
        todasEstacoes.addAll(estacoesMul);
        todasEstacoes.addAll(estacoesLoad);
        todasEstacoes.addAll(estacoesBranches);

        for (EstacaoDeReserva estacao : todasEstacoes) {
            if (estacao.isBusy()) {
                if (estacao.getQj() != null && estacao.getQj().equals(regPrivado)) {
                    estacao.setVj(valor);
                    estacao.setQj(null);
                }
                if (estacao.getQk() != null && estacao.getQk().equals(regPrivado)) {
                    estacao.setVk(valor);
                    estacao.setQk(null);
                }
            }
        }
    }

    /**
     * Fase de Execucao: Inicia execucao de operacoes prontas
     */
    private void execute() {

        List<EstacaoDeReserva> todasEstacoes = new ArrayList<>();
        todasEstacoes.addAll(estacoesAdd);
        todasEstacoes.addAll(estacoesMul);
        todasEstacoes.addAll(estacoesLoad);
        todasEstacoes.addAll(estacoesBranches);

        for (EstacaoDeReserva estacao : todasEstacoes) {
            if (estacao.isBusy()) {
                boolean pronta = estacao.prontaParaExecucao();
                if (estacao.getCiclosRestantes() > 0 && pronta) {
                    ReorderBufferSlot slot = encontrarSlotROB(estacao.getDest());
                    if (slot != null) {
                        slot.setEstado(EstadoInstrucao.EXECUTANDO);
                        if (slot.getCicloExecucao() == -1)
                            slot.setCicloExecucao(cicloAtual);
                        boolean terminou = estacao.executarCiclo();
                        if (terminou) {
                            slot.setCicloEscrita(cicloAtual);
                            // logExecucao.add("Execute: " + estacao.getNome() + " completou execucao");
                            Instrucao inst = slot.getInstrucao();
                            if (inst != null) inst.setEstadoExecucao(2); // executada
                            slot.setEstado(EstadoInstrucao.EXECUTADO);
                        }
                    }
                }
                if (!pronta) {
                    ciclosBolha++;
                    // Log detalhado do motivo da bolha
                    String motivo = "Bolha criada: estacao " + estacao.getNome() + " aguardando operandos ";
                    if (estacao.getQj() != null) motivo += "Qj=" + estacao.getQj() + " ";
                    if (estacao.getQk() != null) motivo += "Qk=" + estacao.getQk();
                    logExecucao.add(motivo.trim());
                }
            }
        }

    }

    private void issue() {
    // Verifica se há instruções para processar
    if (pc >= instrucoes.size()) {
        return;
    }
    
    // Verifica se há espaço no ROB
    if (rob.get(robTail).isBusy()) {
        logExecucao.add("ROB cheio, não foi possivel emitir a instrucao: " + instrucoes.get(pc).toString());
        ciclosBolha++;
        return;
    }
    
    Instrucao inst = instrucoes.get(pc);
    EstacaoDeReserva estacao = encontrarEstacaoLivre(inst.getOp());
    
    // Verifica se há estação de reserva disponível
    if (estacao == null) {
        logExecucao.add("Nenhuma estacao de reserva disponivel, não foi possivel emitir a instrucao: " + inst.toString());
        ciclosBolha++;
        return;
    }
    
    // Emite a instrução
    emitirInstrucao(inst, estacao);
    pc++;
    atualizarEstadoInstrucao(inst);
}

/**
 * Emite uma instrução para execução
 */
private void emitirInstrucao(Instrucao inst, EstacaoDeReserva estacao) {
    ReorderBufferSlot slot = rob.get(robTail);
    
    // Configura slot do ROB
    configurarSlotROB(slot, inst);
    
    // Verifica dependências de dados e configura operandos
    verificaDependenciaVDD(inst, estacao);
    
    // Configura a estação de reserva
    configurarEstacaoReserva(estacao, inst);
    
    // Processa renomeação de registradores se necessário
    if (inst.podeEscrever() && !filaRegistradoresLivres.isEmpty()) {
        processarRenomeacaoRegistradores(inst, slot, estacao);
    } else {
        // Para instruções que não escrevem em registradores
        String regPrivado = filaRegistradoresLivres.poll();
        estacao.setDest(regPrivado);
        slot.setRegistradorRenomeado(regPrivado);
    }
    
    // Avança a cauda do ROB
    robTail = (robTail + 1) % TAMANHO_ROB;
}

/**
 * Configura o slot do ROB para uma nova instrução
 */
private void configurarSlotROB(ReorderBufferSlot slot, Instrucao inst) {
    slot.setBusy(true);
    slot.setPronto(false);
    slot.setInstrucao(inst);
    slot.setEstado(EstadoInstrucao.PROCESSANDO);
    slot.setCicloIssue(cicloAtual);
}

/**
 * Configura a estação de reserva para executar a instrução
 */
private void configurarEstacaoReserva(EstacaoDeReserva estacao, Instrucao inst) {
    estacao.setBusy(true);
    estacao.setOp(inst.getOp());
    estacao.setCiclosRestantes(inst.getCiclosDuracao());
    
    // Configura imediato se presente
    int imediato = inst.getImediato();
    if (imediato != 0) {
        estacao.setImediato(imediato);
    }
}

/**
 * Processa o renomeação de registradores para instruções que escrevem em registradores
 */
private void processarRenomeacaoRegistradores(Instrucao inst, ReorderBufferSlot slot, EstacaoDeReserva estacao) {
    String regPublico = inst.getRd();
    String regPrivado = filaRegistradoresLivres.poll();
    
    // Inicializa o valor no banco privado
    bancoPrivado.put(regPrivado, bancoRegistradores.get(regPublico));
    
    // Configura os mapeamentos
    slot.setRegistradorRenomeado(regPrivado);
    slot.setRegistradorPublico(regPublico);
    estacao.setDest(regPrivado);
    mapaRenomeacao.put(inst.getRd(), regPrivado);
}

/**
 * Atualiza o estado da instrução após emissão
 */
private void atualizarEstadoInstrucao(Instrucao inst) {
    if (inst.getEstadoExecucao() > 0) {
        inst.addExecucao();                        
    }
    inst.setEstadoExecucao(1); // marcada como emitida
}

    // private void issue() {
    //     if (pc < instrucoes.size()) {
    //         if (!rob.get(robTail).isBusy()) {
    //             Instrucao inst = instrucoes.get(pc);
    //             EstacaoDeReserva estacao = encontrarEstacaoLivre(inst.getOp());
    //             if (estacao != null) {
    //                 if (inst.podeEscrever() && !filaRegistradoresLivres.isEmpty()) {
    //                     ReorderBufferSlot slot = rob.get(robTail);
    //                     slot.setBusy(true);
    //                     slot.setPronto(false);
    //                     slot.setInstrucao(inst);
    //                     slot.setEstado(EstadoInstrucao.PROCESSANDO);
    //                     slot.setCicloIssue(cicloAtual);
    //                     String regPublico = inst.getRd();
    //                     verificaDependenciaVDD(inst, estacao);
    //                     String regPrivado = filaRegistradoresLivres.poll();
    //                     bancoPrivado.put(regPrivado, bancoRegistradores.get(regPublico));
    //                     slot.setRegistradorRenomeado(regPrivado);
    //                     slot.setRegistradorPublico(regPublico);
    //                     estacao.setDest(regPrivado);
    //                     mapaRenomeacao.put(inst.getRd(), regPrivado);
    //                     int imediato = inst.getImediato();
    //                     if (imediato != 0) {
    //                         estacao.setImediato(imediato);
    //                     }
    //                     estacao.setBusy(true);
    //                     estacao.setOp(inst.getOp());
    //                     estacao.setCiclosRestantes(inst.getCiclosDuracao());
    //                     robTail = (robTail + 1) % TAMANHO_ROB;
    //                 } else {
    //                     ReorderBufferSlot slot = rob.get(robTail);
    //                     slot.setBusy(true);
    //                     slot.setInstrucao(inst);
    //                     slot.setPronto(false);
    //                     slot.setEstado(EstadoInstrucao.PROCESSANDO);
    //                     slot.setCicloIssue(cicloAtual);
    //                     verificaDependenciaVDD(inst, estacao);
    //                     int imediato = inst.getImediato();
    //                     if (imediato != 0) {
    //                         estacao.setImediato(imediato);
    //                     }
    //                     String regPrivado = filaRegistradoresLivres.poll();
    //                     estacao.setDest(regPrivado);
    //                     slot.setRegistradorRenomeado(regPrivado);
    //                     estacao.setBusy(true);
    //                     estacao.setOp(inst.getOp());
    //                     estacao.setCiclosRestantes(inst.getCiclosDuracao());
    //                     robTail = (robTail + 1) % TAMANHO_ROB;
    //                 }
    //                 pc++;
    //                 if (inst.getEstadoExecucao() > 0) {
    //                     inst.addExecucao();                        
    //                 }
    //                 inst.setEstadoExecucao(1); // lida
    //             } else {
    //                 logExecucao.add("Nenhuma estacao de reserva disponivel, não foi possivel emitir a instrucao: "
    //                         + inst.toString());
    //                 ciclosBolha++;
    //             }
    //         } else {
    //             logExecucao.add("ROB cheio, não foi possivel emitir a instrucao: " + instrucoes.get(pc).toString());
    //             ciclosBolha++;
    //         }
    //     }
    // }

    /*
     * @brief Essa funcao verifica se ha dependências de dados entre a instrucao
     * atual e alguma instrucao ROB
     * e devolve a posicao no ROB em que ha esse conflito.
     */
    private void verificaDependenciaVDD(Instrucao inst, EstacaoDeReserva estacao) {
        // System.out.println("Verificando dependência VDD para: " + reg1 + ", " +
        // reg2);
        // Verifica se a instrucao depende de outra que ainda não foi completada
        String reg1 = inst.getReg1();
        String reg2 = inst.getReg2();
        ReorderBufferSlot conflito1 = null, conflito2 = null;
        for (int i = robHead; i != robTail; i = (i + 1) % TAMANHO_ROB) {
            if (rob.get(i).isBusy()) {
                String regPublico = rob.get(i).getRegistradorPublico();
                if (regPublico != null && regPublico.equals(reg1)) {
                    if (conflito1 != null) {
                        if (conflito1.getCicloIssue() < rob.get(i).getCicloIssue()) {
                            conflito1 = rob.get(i);
                        }
                    } else {
                        conflito1 = rob.get(i);
                    }
                } else if (regPublico != null && regPublico.equals(reg2)) {
                    if (conflito2 != null) {
                        if (conflito2.getCicloIssue() < rob.get(i).getCicloIssue()) {
                            conflito2 = rob.get(i);
                        }
                    } else {
                        conflito2 = rob.get(i);
                    }
                }
            }
        }
        if (conflito1 != null) {
            logExecucao.add("Conflito verdadeiro: " + "instrucao " + inst.toString() + " em conflito com " + conflito1.getInstrucao().toString() + " em " + reg1);
            if (conflito1.isPronto()) {
                estacao.setVj(bancoPrivado.get(conflito1.getRegistradorRenomeado()));
            } else {
                estacao.setQj(conflito1.getRegistradorRenomeado());
            }
        } else {
            estacao.setVj(bancoRegistradores.get(reg1));
        }
        if (conflito2 != null) {
            logExecucao.add("Conflito verdadeiro: " + "instrucao " + inst.toString() + " em conflito com " + conflito2.getInstrucao().toString() + " em " + reg2);
            if (conflito2.isPronto()) {
                estacao.setVk(bancoPrivado.get(conflito2.getRegistradorRenomeado()));
            } else {
                estacao.setQk(conflito2.getRegistradorRenomeado());
            }
        } else {
            estacao.setVk(bancoRegistradores.get(reg2));
        }
    }

    /**
     * Encontra uma estacao de reserva livre para a operacao
     */
    private EstacaoDeReserva encontrarEstacaoLivre(OpCode op) {
        List<EstacaoDeReserva> estacoes;

        // System.out.println("Encontrando estacao livre para a operacao: " + op);

        if (op.isMemoryOperation()) {
            estacoes = estacoesLoad;
        } else if (op.isMultiplyDivide()) {
            estacoes = estacoesMul;
        } else if (op.isBranch()) {
            estacoes = estacoesBranches;
        } else {
            estacoes = estacoesAdd;
        }

        for (EstacaoDeReserva estacao : estacoes) {
            // System.out.println("Verificando estacao: " + estacao.getNome() + " - Busy: "
            // + estacao.isBusy());
            if (!estacao.isBusy()) {
                return estacao;
            }
        }

        return null;
    }
    

    /**
     * Fase de Commit: Retira instrucoes da cabeça do ROB
     */

    /**
 * Fase de Commit: Retira instrucoes da cabeça do ROB em ordem
 * Garante que as instruções sejam cometidas na ordem original do programa
 */
private void commit() {
    ReorderBufferSlot slot = rob.get(robHead);
    
    // Verifica se o slot está pronto para commit
    if (!podeFazerCommit(slot)) {
        return;
    }
    
    Instrucao instrucao = slot.getInstrucao();
    slot.setCicloCommit(cicloAtual);
    
    // Atualiza estado da instrução
    if (instrucao != null) {
        instrucao.setEstadoExecucao(4); // marcada como commitada
    }
    
    // Processa commit baseado no tipo de instrução
    if (instrucaoPodeEscreverRegistrador(instrucao, slot)) {
        commitarInstrucaoComEscrita(instrucao, slot);
    } else {
        commitarInstrucaoSemEscrita(instrucao, slot);
    }
    
    // Limpa o slot e avança a cabeça do ROB
    finalizarCommit(slot);
}

/**
 * Verifica se um slot do ROB está pronto para commit
 */
private boolean podeFazerCommit(ReorderBufferSlot slot) {
    return slot.isBusy() && 
           slot.isPronto() && 
           slot.getCicloCommit() != cicloAtual;
}

/**
 * Verifica se a instrução escreve em registrador e tem dados necessários
 */
private boolean instrucaoPodeEscreverRegistrador(Instrucao instrucao, ReorderBufferSlot slot) {
    return instrucao != null &&
           instrucao.podeEscrever() && 
           slot.getRegistradorPublico() != null &&
           slot.getResultado() != null;
}

/**
 * Processa commit de instruções que escrevem em registradores
 */
private void commitarInstrucaoComEscrita(Instrucao instrucao, ReorderBufferSlot slot) {
    String registradorPublico = slot.getRegistradorPublico();
    String registradorPrivado = slot.getRegistradorRenomeado();
    Float resultado = slot.getResultado();
    
    // Atualiza o valor no banco público de registradores
    bancoRegistradores.put(registradorPublico, resultado);
    
    // Libera o registrador privado para reutilização
    filaRegistradoresLivres.offer(registradorPrivado);
    
    // Remove o mapeamento do registrador público
    mapaRenomeacao.remove(registradorPublico);
    
    // Log do commit
    logExecucao.add("Commit: " + instrucao + " -> " + registradorPublico + " = " + resultado);
}

/**
 * Processa commit de instruções que não escrevem em registradores
 */
private void commitarInstrucaoSemEscrita(Instrucao instrucao, ReorderBufferSlot slot) {
    // Apenas registra o commit no log
    logExecucao.add("Commit: " + instrucao);
}

/**
 * Finaliza o processo de commit limpando o slot e atualizando estatísticas
 */
private void finalizarCommit(ReorderBufferSlot slot) {
    // Limpa o slot do ROB
    slot.limpar();
    
    // Avança a cabeça do ROB de forma circular
    robHead = (robHead + 1) % TAMANHO_ROB;
    
    // Atualiza estatísticas
    instrucoesExecutadas++;
}
    // private void commit() {

    //     ReorderBufferSlot slot = rob.get(robHead);

    //     if (slot.isBusy() && slot.isPronto() && slot.getCicloCommit() != cicloAtual) {
    //         Instrucao inst = slot.getInstrucao();
    //         slot.setCicloCommit(cicloAtual);
    //         if (inst != null) inst.setEstadoExecucao(4); // commitada
    //         // Atualiza banco publico se a instrucao escreve em registrador
    //         if (inst.podeEscrever() && slot.getRegistradorPublico() != null) {
    //             String regPub = slot.getRegistradorPublico();
    //             // String regPrivAntigo = mapaRenomeacao.get(regPub);
    //             String regPriv = slot.getRegistradorRenomeado();

    //             // Atualiza o valor no banco publico
    //             bancoRegistradores.put(regPub, slot.getResultado());
    //             filaRegistradoresLivres.offer(regPriv);
    //             mapaRenomeacao.remove(regPub);
    //             logExecucao.add("Commit: " + inst + " -> " + regPub + " = " + slot.getResultado());
    //         } else {
    //             logExecucao.add("Commit: " + inst);
    //         }

    //         slot.limpar();
    //         robHead = (robHead + 1) % TAMANHO_ROB;
    //         instrucoesExecutadas++;
    //     }

    // }

    /**
     * Verifica se o ROB esta vazio
     */
    private boolean robVazio() {
        boolean resultado = true;
        for (ReorderBufferSlot slot : rob) {
            if (slot.isBusy()) {
                resultado = false;
            }
        }
        return resultado;
    }

    // Funcoes Requisitadas pela GUI

    /**
     * Executa a simulacao completa
     */
    public void executarCompleto() {
        while (!simulacaoCompleta) {
            proximoCiclo();

            // Protecao contra loop infinito
            if (cicloAtual > 10000) {
                System.err.println("Simulacao interrompida: muitos ciclos");
                break;
            }
        }
    }

    /**
     * Carrega instrucoes de um arquivo
     */
    public void carregarInstrucoes(String nomeArquivo) throws IOException {
        reiniciar();
        instrucoes = InstructionParser.lerInstrucoes(nomeArquivo);
        pc = 0;
        logExecucao.add("Carregadas " + instrucoes.size() + " instrucoes do arquivo: " + nomeArquivo);
    }

    /**
     * Calcula o IPC (Instructions Per Cycle)
     */
    public double calcularIPC() {
//passo 40
        if (totalCiclos == 0)
            return 0.0;
        return (double) instrucoesExecutadas / totalCiclos;
    }

    /*
     * Retorna o Buffer de Reordenamento
     */
    public List<ReorderBufferSlot> getReorderBufferState() {
//passo 23
        return rob;
    }

    /*
     * Retorna as Estacoes de Reserva
     */
    public List<EstacaoDeReserva> getReservationStationsState() {
//passo 22
        List<EstacaoDeReserva> todas = new ArrayList<>();
//passo 23
        todas.addAll(estacoesAdd);
        todas.addAll(estacoesMul);
        todas.addAll(estacoesLoad);
        todas.addAll(estacoesBranches);
//passo 24
        return todas;
    }

    /*
     * Retorna os estatus dos registradores
     */
    public Map<String, Object> getRegisterStatus() {
//passo 31
        Map<String, Object> status = new HashMap<>();
//passo 32
        status.put("publico", new HashMap<>(bancoRegistradores));
        status.put("fisico", new HashMap<>(bancoPrivado));
        status.put("mapeamento", new HashMap<>(mapaRenomeacao));
        status.put("livres", new ArrayList<>(filaRegistradoresLivres));
//passo 33
        return status;
    }

    // Getters para estatisticas
    public int getCicloAtual() {
//passo 38
        return cicloAtual;
    }

    public int getTotalCiclos() {
        return totalCiclos;
    }

    public int getCiclosBolha() {
        return ciclosBolha;
    }

    public boolean isSimulacaoCompleta() {
        return simulacaoCompleta;
    }

    public int getInstrucoesExecutadas() {
        return instrucoesExecutadas;
    }

    public List<Instrucao> getInstrucoes() {
        return instrucoes;
    }

    public List<String> getLogExecucao() {
//passo 46
        return logExecucao;
    }

    public int getPc() {
        return pc;
    }

    public int getTotalInstrucoes() {
        return instrucoes.size();
    }

}
