package gui;

import simulador.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Interface grafica principal do simulador Tomasulo
 */
public class SimuladorMain extends JFrame {
    private Simulador simulador;
    
    // Componentes da interface
    private JTable tabelaInstrucoes;
    private JTable tabelaEstacoes;
    private JTable tabelaROB;
    private JTable tabelaRegistradores;
    private JTextArea areaLog;
    private JLabel labelCiclo;
    private JLabel labelIPC;
    private JLabel labelInstrucoes;
    private JLabel labelCiclosBolha;
    private JButton btnCarregar;
    private JButton btnProximoCiclo;
    private JButton btnExecutarCompleto;
    private JButton btnReiniciar;
    
    // Modelos das tabelas
    private DefaultTableModel modeloInstrucoes;
    private DefaultTableModel modeloEstacoes;
    private DefaultTableModel modeloROB;
    private DefaultTableModel modeloRegistradores;
    
    // Cores do tema escuro
    private Color corFundo = new Color(25, 25, 35);
    private Color corPainel = new Color(40, 40, 55);
    private Color corDestaque = new Color(0, 184, 255);
    private Color corTexto = new Color(240, 240, 255);
    private Color corSucesso = new Color(76, 175, 80);
    private Color corAlerta = new Color(255, 152, 0);
    private Color corErro = new Color(244, 67, 54);

    public SimuladorMain() {
        simulador = new Simulador();
        aplicarTemaPersonalizado();
        inicializarInterface();
        atualizarInterface();
    }
    
    private void aplicarTemaPersonalizado() {
        try {
            // Configuracoes basicas para forcar o tema escuro
            UIManager.put("Panel.background", corFundo);
            UIManager.put("Table.background", corPainel);
            UIManager.put("Table.foreground", corTexto);
            UIManager.put("Table.selectionBackground", corDestaque);
            UIManager.put("Table.selectionForeground", Color.WHITE);
            UIManager.put("Table.gridColor", new Color(80, 80, 100));
            
            UIManager.put("TableHeader.background", new Color(50, 50, 70));
            UIManager.put("TableHeader.foreground", corTexto);
            UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 12));
            
            UIManager.put("TextField.background", corPainel);
            UIManager.put("TextField.foreground", corTexto);
            UIManager.put("TextField.caretForeground", corDestaque);
            
            UIManager.put("TextArea.background", new Color(20, 20, 30));
            UIManager.put("TextArea.foreground", corSucesso);
            UIManager.put("TextArea.selectionBackground", corDestaque);
            
            UIManager.put("Button.background", new Color(60, 60, 80));
            UIManager.put("Button.foreground", corTexto);
            UIManager.put("Button.focus", corDestaque);
            UIManager.put("Button.select", corDestaque);
            
            UIManager.put("Label.foreground", corTexto);
            UIManager.put("TitledBorder.titleColor", corDestaque);
            
            UIManager.put("ScrollBar.background", corPainel);
            UIManager.put("ScrollBar.thumb", new Color(80, 80, 100));
            UIManager.put("ScrollBar.thumbDarkShadow", corDestaque);
            
            UIManager.put("OptionPane.background", corFundo);
            UIManager.put("OptionPane.messageForeground", corTexto);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void inicializarInterface() {
        setTitle("Simulador Tomasulo - Visual Moderno");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Configurar fundo personalizado
        getContentPane().setBackground(corFundo);
        
        // Painel de log no topo
        JPanel painelLog = criarPainelLog();
        add(painelLog, BorderLayout.NORTH);

        // Painel central com tabelas
        JPanel painelCentral = criarPainelCentral();
        add(painelCentral, BorderLayout.CENTER);

        // Painel de controles na parte inferior
        JPanel painelControles = criarPainelControles();
        add(painelControles, BorderLayout.SOUTH);

        // Configuracoes da janela
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
    
    private JPanel criarPainelControles() {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        painel.setBackground(new Color(35, 35, 50));
        painel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 80), 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        
        // Labels de estatisticas
        labelCiclosBolha = criarLabelPersonalizado("Bolhas: 0");
        labelInstrucoes = criarLabelPersonalizado("Instrucoes: 0/0");
        labelIPC = criarLabelPersonalizado("IPC: 0.00");
        labelCiclo = criarLabelPersonalizado("Ciclo: 0");

        // Separador
        painel.add(criarSeparadorVertical());
        
        // Adicionar labels
        painel.add(labelCiclosBolha);
        painel.add(labelInstrucoes);
        painel.add(labelIPC);
        painel.add(labelCiclo);
        
        // Separador entre labels e botoes
        painel.add(criarSeparadorVertical());
        
        // Botoes
        btnReiniciar = criarBotaoPersonalizado("Reiniciar", new Color(200, 100, 255));
        btnReiniciar.addActionListener(e -> reiniciar());
        btnReiniciar.setEnabled(false);

        btnExecutarCompleto = criarBotaoPersonalizado("Executar Tudo", corAlerta);
        btnExecutarCompleto.addActionListener(e -> executarCompleto());
        btnExecutarCompleto.setEnabled(false);
        
        btnProximoCiclo = criarBotaoPersonalizado("Proximo Ciclo", corSucesso);
        btnProximoCiclo.addActionListener(e -> proximoCiclo());
        btnProximoCiclo.setEnabled(false);
        
        btnCarregar = criarBotaoPersonalizado("Carregar Arquivo", corDestaque);
        btnCarregar.addActionListener(e -> carregarArquivo());

        // Adicionar botoes
        painel.add(btnReiniciar);
        painel.add(btnExecutarCompleto);
        painel.add(btnProximoCiclo);
        painel.add(btnCarregar);
        
        return painel;
    }
    
    private JButton criarBotaoPersonalizado(String texto, Color corFundo) {
        JButton botao = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (!isEnabled()) {
                    g2.setColor(new Color(80, 80, 80));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                } else if (getModel().isPressed()) {
                    g2.setColor(corFundo.darker());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                } else if (getModel().isRollover()) {
                    g2.setColor(corFundo.brighter());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                } else {
                    g2.setColor(corFundo);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                
                super.paintComponent(g);
            }
            
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isEnabled() ? corFundo.brighter() : new Color(100, 100, 100));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 10, 10);
            }
        };
        
        botao.setFont(new Font("Segoe UI", Font.BOLD, 12));
        botao.setForeground(Color.WHITE);
        botao.setBorderPainted(false);
        botao.setContentAreaFilled(false);
        botao.setFocusPainted(false);
        botao.setPreferredSize(new Dimension(140, 35));
        return botao;
    }
    
    private JLabel criarLabelPersonalizado(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(corTexto);
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(70, 70, 90), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        label.setBackground(new Color(45, 45, 65));
        label.setOpaque(true);
        return label;
    }
    
    private JSeparator criarSeparadorVertical() {
        JSeparator separador = new JSeparator(SwingConstants.VERTICAL);
        separador.setForeground(new Color(90, 90, 110));
        separador.setPreferredSize(new Dimension(1, 25));
        return separador;
    }
    
    private JPanel criarPainelCentral() {
        JPanel painel = new JPanel(new GridLayout(2, 2, 8, 8));
        painel.setBackground(corFundo);
        painel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Ordem das tabelas no grid
        painel.add(criarPainelTabela("Registradores", 
            new String[]{"Reg", "Busy", "Fisico", "Valor"}, 
            false));
        painel.add(criarPainelTabela("ROB", 
            new String[]{"Entry", "Busy", "Instrucao", "Estado", "Destino", "Valor"}, 
            false));
        painel.add(criarPainelTabela("Estacoes", 
            new String[]{"Nome", "Busy", "Op", "Vj", "Vk", "Qj", "Qk", "Dest"}, 
            false));
        painel.add(criarPainelTabela("Instrucoes", 
            new String[]{"Instrucao", "Issue", "Execute", "Write", "Commit"}, 
            true));
        
        return painel;
    }
    
    private JPanel criarPainelTabela(String titulo, String[] colunas, boolean isInstrucoes) {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(corFundo);
        
        // Titulo personalizado
        JLabel lblTitulo = new JLabel(" " + titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitulo.setForeground(corDestaque);
        lblTitulo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, corDestaque),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        lblTitulo.setBackground(new Color(40, 40, 60));
        lblTitulo.setOpaque(true);
        painel.add(lblTitulo, BorderLayout.NORTH);
        
        // Criar modelo de tabela
        DefaultTableModel modelo = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Criar tabela personalizada
        JTable tabela = new JTable(modelo) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                
                if (!isRowSelected(row)) {
                    if (row % 2 == 0) {
                        c.setBackground(new Color(45, 45, 65));
                    } else {
                        c.setBackground(new Color(50, 50, 70));
                    }
                    c.setForeground(corTexto);
                }
                
                // Destacar celulas com checkmarks
                Object value = getValueAt(row, column);
                if (value != null && value.toString().equals("✓")) {
                    c.setForeground(corSucesso);
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                }
                
                return c;
            }
        };
        
        // Configurar a tabela
        tabela.setFont(new Font("Consolas", Font.PLAIN, 11));
        tabela.setRowHeight(22);
        tabela.setSelectionBackground(corDestaque);
        tabela.setSelectionForeground(Color.WHITE);
        tabela.setGridColor(new Color(70, 70, 90));
        tabela.setShowGrid(true);
        tabela.getTableHeader().setBackground(new Color(55, 55, 75));
        tabela.getTableHeader().setForeground(corTexto);
        tabela.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        
        // Atribuir a variavel correta
        if (titulo.contains("Instrucoes")) {
            tabelaInstrucoes = tabela;
            modeloInstrucoes = modelo;
        } else if (titulo.contains("Estacoes")) {
            tabelaEstacoes = tabela;
            modeloEstacoes = modelo;
        } else if (titulo.contains("ROB")) {
            tabelaROB = tabela;
            modeloROB = modelo;
        } else if (titulo.contains("Registradores")) {
            tabelaRegistradores = tabela;
            modeloRegistradores = modelo;
        }
        
        JScrollPane scroll = new JScrollPane(tabela);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 80)));
        scroll.getViewport().setBackground(corPainel);
        
        painel.add(scroll, BorderLayout.CENTER);
        
        return painel;
    }
    
    private JPanel criarPainelLog() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(corFundo);
        painel.setPreferredSize(new Dimension(0, 180));
        
        JLabel lblTitulo = new JLabel(" Log de Execucao");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitulo.setForeground(corDestaque);
        lblTitulo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, corDestaque),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        lblTitulo.setBackground(new Color(40, 40, 60));
        lblTitulo.setOpaque(true);
        painel.add(lblTitulo, BorderLayout.NORTH);
        
        areaLog = new JTextArea();
        areaLog.setEditable(false);
        areaLog.setFont(new Font("Consolas", Font.PLAIN, 11));
        areaLog.setBackground(new Color(15, 15, 25));
        areaLog.setForeground(new Color(100, 255, 150));
        areaLog.setCaretColor(Color.WHITE);
        
        JScrollPane scroll = new JScrollPane(areaLog);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 80)));
        scroll.getViewport().setBackground(new Color(15, 15, 25));
        
        painel.add(scroll, BorderLayout.CENTER);
        
        return painel;
    }
    
    // ========== METODOS DE FUNCIONALIDADE ==========
    
    private void carregarArquivo() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt");
            }
            @Override
            public String getDescription() {
                return "Arquivos de texto (*.txt)";
            }
        });
        
        // Personalizar file chooser
        fileChooser.setBackground(corPainel);
        fileChooser.setForeground(corTexto);

        int resultado = fileChooser.showOpenDialog(this);
        
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            try {
                simulador.carregarInstrucoes(arquivo.getAbsolutePath());
                btnProximoCiclo.setEnabled(true);
                btnExecutarCompleto.setEnabled(true);
                btnReiniciar.setEnabled(true);
                atualizarInterface();
                
                JOptionPane.showMessageDialog(this, 
                    "Arquivo carregado com sucesso!\n" + 
                    simulador.getTotalInstrucoes() + " instrucoes carregadas.",
                    "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "Erro ao carregar arquivo:\n" + e.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void proximoCiclo() {
        simulador.proximoCiclo();
        atualizarInterface();
        
        if (simulador.isSimulacaoCompleta()) {
            btnProximoCiclo.setEnabled(false);
            btnExecutarCompleto.setEnabled(false);
            JOptionPane.showMessageDialog(this,
                String.format("Simulacao completa!\n\nEstatisticas:\n" +
                    "• Total de Ciclos: %d\n" +
                    "• Instrucoes Executadas: %d\n" +
                    "• Ciclos de Bolha: %d\n" +
                    "• IPC: %.2f",
                    simulador.getTotalCiclos(),
                    simulador.getInstrucoesExecutadas(),
                    simulador.getCiclosBolha(),
                    simulador.calcularIPC()),
                "Simulacao Completa", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void executarCompleto() {
        btnProximoCiclo.setEnabled(false);
        btnExecutarCompleto.setEnabled(false);
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                simulador.executarCompleto();
                return null;
            }
            
            @Override
            protected void done() {
                atualizarInterface();
                JOptionPane.showMessageDialog(SimuladorMain.this,
                    String.format("Simulacao completa!\n\nEstatisticas:\n" +
                        "• Total de Ciclos: %d\n" +
                        "• Instrucoes Executadas: %d\n" +
                        "• Ciclos de Bolha: %d\n" +
                        "• IPC: %.2f",
                        simulador.getTotalCiclos(),
                        simulador.getInstrucoesExecutadas(),
                        simulador.getCiclosBolha(),
                        simulador.calcularIPC()),
                    "Simulacao Completa", JOptionPane.INFORMATION_MESSAGE);
            }
        };
        
        worker.execute();
    }
    
    private void reiniciar() {
        simulador.reiniciar();
        btnProximoCiclo.setEnabled(true);
        btnExecutarCompleto.setEnabled(true);
        btnReiniciar.setEnabled(true);
        atualizarInterface();
    }
    
    private void atualizarInterface() {
        atualizarTabelaInstrucoes();
        atualizarTabelaEstacoes();
        atualizarTabelaROB();
        atualizarTabelaRegistradores();
        atualizarEstatisticas();
        atualizarLog();
    }
    
    private void atualizarTabelaInstrucoes() {
        modeloInstrucoes.setRowCount(0);
        List<Instrucao> instrucoes = simulador.getInstrucoes();

        for (int i = 0; i < simulador.getTotalInstrucoes(); i++) {
            Instrucao instrucao = instrucoes.get(i);
            String instrucaoString = (i + 1) + ": " + instrucao.toString() + ((instrucao.getQtdeExecucoes() == 0) ? "" : " (" + instrucao.getQtdeExecucoes() + ")");
            String issue = "-";
            String execute = "-";
            String writeResult = "-";
            String commit = "-";

            if (instrucao.getEstadoExecucao() == -1) {
                instrucaoString += " (Pulada)";
            } else {
                if(instrucao.getEstadoExecucao() >= 1) {
                    issue = "✓";
                }
                if(instrucao.getEstadoExecucao() >= 2) {
                    execute = "✓";
                }
                if(instrucao.getEstadoExecucao() >= 3) {
                    writeResult = "✓";
                }
                if(instrucao.getEstadoExecucao() >= 4) {
                    commit = "✓";
                }
            }
            
            modeloInstrucoes.addRow(new Object[]{instrucaoString, issue, execute, writeResult, commit});
        }
    }
    
    private void atualizarTabelaEstacoes() {
        modeloEstacoes.setRowCount(0);
        List<EstacaoDeReserva> estacoes = simulador.getReservationStationsState();

        for (EstacaoDeReserva estacao : estacoes) {
            Object[] linha = {
                estacao.getNome(),
                estacao.isBusy() ? "Sim" : "Nao",
                estacao.getOp() != null ? estacao.getOp().getNome() : "-",
                estacao.getVj() != null ? String.format("%.2f", estacao.getVj()) : "-",
                estacao.getVk() != null ? String.format("%.2f", estacao.getVk()) : "-",
                estacao.getQj() != null ? estacao.getQj() : "-",
                estacao.getQk() != null ? estacao.getQk() : "-",
                estacao.getDest() != null ? estacao.getDest() : "-"
            };
            modeloEstacoes.addRow(linha);
        }
    }
    
    private void atualizarTabelaROB() {
        modeloROB.setRowCount(0);
        List<ReorderBufferSlot> rob = simulador.getReorderBufferState();

        for (int i = 0; i < rob.size(); i++) {
            ReorderBufferSlot slot = rob.get(i);
            Object[] linha = {
                i,
                slot.isBusy() ? "Sim" : "Nao",
                slot.getInstrucao() != null ? slot.getInstrucao().toString() : "-",
                slot.isBusy() ? slot.getEstado().getDescricao() : "-",
                slot.getRegistradorPublico() != null ? slot.getRegistradorPublico() : "-",
                slot.isPronto() && slot.getResultado() != null ? 
                    String.format("%.2f", slot.getResultado()) : "-"
            };
            modeloROB.addRow(linha);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void atualizarTabelaRegistradores() {
        modeloRegistradores.setRowCount(0);
        Map<String, Object> status = simulador.getRegisterStatus();
        Map<String, Float> publico = (Map<String, Float>) status.get("publico");
        Map<String, String> mapeamento = (Map<String, String>) status.get("mapeamento");

        for (String reg : publico.keySet()) {
            String regFisico = mapeamento.get(reg);
            Float valor = publico.get(reg);
            
            boolean busy = false;
            List<ReorderBufferSlot> rob = simulador.getReorderBufferState();
            for (ReorderBufferSlot slot : rob) {
                if (slot.isBusy() && reg.equals(slot.getRegistradorPublico()) && !slot.isPronto()) {
                    busy = true;
                    break;
                }
            }
            
            Object[] linha = {
                reg,
                busy ? "Sim" : "Nao",
                regFisico,
                String.format("%.2f", valor)
            };
            modeloRegistradores.addRow(linha);
        }
    }
    
    private void atualizarEstatisticas() {
        int cicloAtual = simulador.getCicloAtual();
        int ciclosBolha = simulador.getCiclosBolha();
        int instrucoesExecutadas = simulador.getInstrucoesExecutadas();
        int totalInstrucoes = simulador.getTotalInstrucoes();
        double ipc = simulador.calcularIPC();
        
        labelCiclo.setText("Ciclo: " + (cicloAtual + 1));
        labelIPC.setText(String.format("IPC: %.2f", ipc));
        labelInstrucoes.setText(String.format("Instrucoes: %d/%d", instrucoesExecutadas, totalInstrucoes));
        labelCiclosBolha.setText("Bolhas: " + ciclosBolha);
    }
    
    private void atualizarLog() {
        StringBuilder log = new StringBuilder();
        List<String> logs = simulador.getLogExecucao();

        int inicio = Math.max(0, logs.size() - 100);
        for (int i = inicio; i < logs.size(); i++) {
            log.append(logs.get(i)).append("\n");
        }
        
        areaLog.setText(log.toString());
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SimuladorMain().setVisible(true);
        });
    }
}