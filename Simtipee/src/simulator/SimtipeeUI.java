package simulator;

import tcp_protocols.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SimtipeeUI extends JFrame{

    private JTextField simula;
    private JLabel hostsLabel, bufferLabel, pValueLabel, timeoutLabel, gValueLabel, welchLabel, timeLabel, simulaLabel;
    private JTextField host, pValue, timeout, buffer, gValue, warmUpValue, timeValue;
    private static JButton start, exit;

    private JRadioButton AIMD, TAHOE, RENO;
    private ButtonGroup group;


    /* Logical value to end simulation */
    private static final double alpha = 0.05;
    private static final double qalpha = 1.959964;
    private static boolean valid = false;
    private static int l;
    private static LocalDateTime timeOut;
    private static int end;

    private static String outPath = "out.csv";

    private static ArrayList<Main> T;
    private static ArrayList<List<Double>> runX;
    private static ArrayList<List<Double>> runRST;
    private static ArrayList<List<Double>> runHost;


    private static ArrayList<Double> xDist = new ArrayList<>();
    private static ArrayList<Double> hostDist = new ArrayList<>();
    private static ArrayList<Double> rstDist = new ArrayList<>();


    public SimtipeeUI(){
        super("Simulatore TCP Protocols");
        initComponents();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.pack();
        this.setResizable(false);

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/4-this.getSize().height/2);

        this.setVisible(true);
    }

    private void initComponents() {

        Container pane = this.getContentPane();

        pane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        JPanel protocolPanel = new JPanel();
        protocolPanel.setBorder(BorderFactory.createTitledBorder("TCP Protocols"));
        protocolPanel.setLayout(new GridBagLayout());

        JPanel parametersPanel = new JPanel();
        parametersPanel.setBorder(BorderFactory.createTitledBorder("Parameters"));
        parametersPanel.setLayout(new GridBagLayout());

        JPanel settingsPanel = new JPanel();
        settingsPanel.setBorder(BorderFactory.createTitledBorder("Settings"));
        settingsPanel.setLayout(new GridBagLayout());

        JPanel controlPanel = new JPanel();
        controlPanel.setBorder(BorderFactory.createTitledBorder("Control Buttons"));
        controlPanel.setLayout(new GridBagLayout());


        c.insets = new Insets(3, 3, 3, 3);


        hostsLabel = new JLabel("Hosts");
        host = new JTextField("Valore intero");
        host.setPreferredSize(host.getPreferredSize());

        bufferLabel = new JLabel("Buffer-Size");
        buffer = new JTextField("Valore intero");
        buffer.setPreferredSize(buffer.getPreferredSize());

        pValueLabel = new JLabel("p Value");
        pValue = new JTextField("Valore intero");
        pValue.setPreferredSize(pValue.getPreferredSize());

        timeoutLabel = new JLabel("ERTT");
        timeout = new JTextField("Valore intero");
        timeout.setPreferredSize(timeout.getPreferredSize());
        timeout.setText("-1");

        gValueLabel = new JLabel("g Value");
        gValue = new JTextField("G value");
        gValue.setPreferredSize(timeout.getPreferredSize());

        simulaLabel = new JLabel(("num Sims"));
        simula = new JTextField("n Simulazioni");
        simula.setPreferredSize(simula.getPreferredSize());
        simula.setText("15");



        host.setText("25");
        buffer.setText("25");
        pValue.setText("0.1");
        gValue.setText("0.5");

        // First Row Parameters
        c.gridy = 0;
        c.gridx = 0;
        parametersPanel.add(hostsLabel, c);
        c.gridx = 1;
        parametersPanel.add(bufferLabel, c);
        c.gridx = 2;
        parametersPanel.add(timeoutLabel,c);

        // Second Row Parameters
        c.gridy = 1;
        c.gridx = 0;
        parametersPanel.add(host,c);
        c.gridx = 1;
        parametersPanel.add(buffer,c);
        c.gridx = 2;
        parametersPanel.add(timeout,c);

        // Third Row Parameters
        c.gridy = 2;
        c.gridx = 0;
        parametersPanel.add(pValueLabel,c);
        c.gridx = 1;
        parametersPanel.add(gValueLabel,c);
        c.gridx = 2;
        parametersPanel.add(simulaLabel,c);

        // Fourth Row Parameters
        c.gridy = 3;
        c.gridx = 0;
        parametersPanel.add(pValue,c);
        c.gridx = 1;
        parametersPanel.add(gValue,c);
        c.gridx = 2;
        parametersPanel.add(simula,c);


        // Control Panel - Radio Buttons
        AIMD = new JRadioButton("AIMD");
        RENO = new JRadioButton("RENO");
        TAHOE = new JRadioButton("TAHOE");

        AIMD.setSelected(true);

        group = new ButtonGroup();
        group.add(AIMD);
        group.add(RENO);
        group.add(TAHOE);

        protocolPanel.add(AIMD);
        protocolPanel.add(RENO);
        protocolPanel.add(TAHOE);


        // Settings Panel - Time[ms] & Welch
        timeLabel = new JLabel("Time [ms]");
        welchLabel = new JLabel("Welch l");

        timeValue = new JTextField("Valore di tempo");
        timeValue.setPreferredSize(timeValue.getPreferredSize());
        timeValue.setText("3");

        warmUpValue = new JTextField("Welch value");
        warmUpValue.setPreferredSize(warmUpValue.getPreferredSize());
        warmUpValue.setText("2000");

        c.gridy = 0;
        c.gridx = 0;
        settingsPanel.add(timeLabel, c);
        c.gridx = 1;
        settingsPanel.add(welchLabel, c);

        c.gridy = 1;
        c.gridx = 0;
        settingsPanel.add(timeValue,c);
        c.gridx = 1;
        settingsPanel.add(warmUpValue,c);


        // Control Panel - Start & Exit
        exit = new JButton("Exit");
        exit.addActionListener(e -> System.exit(0));

        start = new JButton("Start simulation");
        start.addActionListener(e -> {
            final TCPConnection protocol;
            if (AIMD.isSelected())
                protocol = new AIMD();
            else
            if (RENO.isSelected())
                protocol = new RENO();
            else
                protocol = new TAHOE();
            double p = Double.parseDouble(pValue.getText());
            int h = Integer.parseInt(host.getText());
            int sim = Integer.parseInt(simula.getText());
            int buf = Integer.parseInt(buffer.getText());

            /* Stima del RoundTripTime*/
            double vrtt = Double.parseDouble(timeout.getText());
            double ertt = (vrtt == -1.0)? 2*Math.min(buf,h)*0.01:vrtt;

            double G = Double.parseDouble(gValue.getText());

            end = Integer.parseInt(timeValue.getText());
            l = Integer.parseInt(warmUpValue.getText());

            System.out.println("Hosts:"+h+" p = "+p+" ERTT: "+ ertt+" buffer: "+buf);
            outPath = protocol.toString()+"_h"+h+"_ch"+buf+"_p"+p+"_g"+G+"_l"+l;

            Runnable launch = () -> launch(h, p, buf,G , ertt, protocol, sim);
            new Thread(launch).start();
            start.setEnabled(false);
        });
        this.getRootPane().setDefaultButton(start);

        c.gridx = 0;
        c.gridy = 0;
        controlPanel.add(start, c);
        c.gridx = 1;
        controlPanel.add(exit, c);


        // Overall Layout
        c.gridx = 0;
        c.gridy = 0;
        pane.add(protocolPanel, c);
        c.gridy = 1;
        pane.add(parametersPanel, c);
        c.gridy = 2;
        pane.add(settingsPanel,c);
        c.gridy = 3;
        pane.add(controlPanel, c);

    }

    public static void main(String[] args) {
        if(args.length >= 1)
            readSimulationsFromArgs(args);
        else{
            System.out.println("Simtipee graphic");
            SimtipeeUI view = new SimtipeeUI();
        }
    }


    public static void launch(int host, double p, int buf, double G, double ertt, TCPConnection protocol,int N_THREAD) {

        double rstMin,rstMean,rstMax,runMean,runMin,runMax;
        double runHostMean,runHostMin,runHostMax;
        double var, sd,delta;
        long n = 1;
        double simulaTime;


        T = new ArrayList<>();
        runX = new ArrayList<>();
        runRST = new ArrayList<>();
        runHost = new ArrayList<>();

        for (int i = 0; i < N_THREAD; i++) {
            int seed = (int) (Math.random() * 197473)+1;
            Main app = new Main(host, p, buf, G, ertt, protocol, seed);
            runX.add(new ArrayList<>());
            runRST.add(new ArrayList<>());
            runHost.add(new ArrayList<>());
            app.start();
            T.add(app);
        }

        timeOut = LocalDateTime.now();
        boolean finished = false;
        String pathOutCsv = protocol.toString()+"_h"+host+"_ch"+buf+"_p"+p+"_g"+G+"_l"+l;

        WriterFile.write("Time;distMin;distMean;distMax;delta;rstMin;rstMean;rstMax;hostMin;hostMean;hostMax;dropped",pathOutCsv);

        boolean started = false;

        while(!started){
            started = true;
            for(Main app: T)
                if(app.getStatistics() == null) started = false;
        }
        System.out.println("\n Simulations started \n");

        while (!finished){
            try {
                Thread.sleep(10);
                List<Statistic> sample = new ArrayList<>();
                xDist.clear();
                rstDist.clear();
                hostDist.clear();

                int i = 0;
                for (Main app : T){
                    sample.add(app.getStatistics());
                    List<Double> column = runX.get(i);
                    List<Double> rstColumn = runRST.get(i);
                    List<Double> hostColumn = runHost.get(i);

                    column.add(app.getStatistics().getHostsThroughput());
                    rstColumn.add(app.getStatistics().getRTT());
                    hostColumn.add(app.getStatistics().getHostThroughput());

                    if(column.size() > l){
                        List<Double> noWarmX = column.subList(l,column.size());
                        List<Double> noWarmRST = rstColumn.subList(l,column.size());

                        xDist.add(noWarmX.stream().mapToDouble(x -> x).average().getAsDouble());
                        rstDist.add(noWarmRST.stream().mapToDouble(x -> x).average().getAsDouble());
                        hostDist.add(hostColumn.stream().mapToDouble(x -> x).average().getAsDouble());
                    }

                    i+= 1;
                }
                if(xDist.size() > 0){
                    n = T.size();

                    /**
                     *
                     * GET THROUGHPUT & EVALUATE DISTRIBUTION
                     */

                    runMean = xDist.stream().mapToDouble(x -> x).average().getAsDouble();
                    runMin = xDist.stream().mapToDouble(x -> x).min().getAsDouble();
                    runMax = xDist.stream().mapToDouble(x -> x).max().getAsDouble();

                    runHostMean = hostDist.stream().mapToDouble(x -> x).average().getAsDouble();
                    runHostMin = hostDist.stream().mapToDouble(x -> x).min().getAsDouble();
                    runHostMax = hostDist.stream().mapToDouble(x -> x).max().getAsDouble();

                    double xn = runHostMean;
                    var = hostDist.stream().mapToDouble(x -> Math.pow((x - xn), 2)).sum() / n;
                    sd = Math.sqrt(var);


                    delta = qalpha * sd / Math.sqrt(n);

                    /**
                     * Check if the interval has a RELATIVE ERROR less than ALPHA
                     */
                    boolean isFinished = isFinished(runHostMean, delta);

                    /**
                     * GET SIMULATIONS TIME & THE NUMBER OF SEGMENTS DROPPED
                     */
                    simulaTime = sample.stream().mapToDouble(x -> x.getSimulationTime()).average().getAsDouble();

                    double dropped;
                    dropped = sample.stream().mapToDouble(x -> x.getNDropped()).average().getAsDouble();

                    /**
                     *
                     * GET RTT & EVALUATE DISTRIBUTION
                     */

                    rstMean = rstDist.stream().mapToDouble(x -> x).average().getAsDouble();
                    rstMin = rstDist.stream().mapToDouble(x -> x).min().getAsDouble();
                    rstMax = rstDist.stream().mapToDouble(x -> x).max().getAsDouble();


                    String data = simulaTime + ";" + runMin + ";" + runMean + ";" + runMax + ";" + delta+";";
                    data += rstMin+";"+rstMean+";"+rstMax+";";
                    data += runHostMin+";"+runHostMean+";"+runHostMax+";"+dropped;
                    WriterFile.write(data, pathOutCsv);

                    if (isFinished) {
                        Main.kill();
                        System.out.println("\n\n ===== KILLED ==== \n\n");
                        finished = true;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        // Wait Threads
        for (Main ti : T) {
            try {
                ti.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //System.out.print("\n\nEnd timeLabel: "+ LocalDateTime.now());
        //start.setEnabled(true);

        System.exit(0);
    }


    public static boolean isFinished(double mean,double delta){

        //return (LocalDateTime.now().compareTo(timeOut.plusMinutes(3)) >= 0);
        if(delta/mean < alpha/(1+alpha)){
            if(!valid){
                System.out.println("Set timeout");
                valid = true;
                timeOut = LocalDateTime.now().plusMinutes(end);
            }
        }

        return (valid && LocalDateTime.now().compareTo(timeOut) > 0);
    }


    public static void readSimulationsFromArgs(String[] args){
        final TCPConnection protocol;
        System.out.println(args.toString());


        if (args[0].equals("AIMD")){
            System.out.println("AIMD");
            protocol = new AIMD();
        }
        else if (args[0].equals("RENO")){
            System.out.println("RENO");
            protocol = new RENO();
        }
        else{
            System.out.println("TAHOE");
            protocol = new TAHOE();
        }

        int h = Integer.parseInt(args[1]);
        int buf = Integer.parseInt(args[2]);
        double p = Double.parseDouble(args[3]);
        double G = Double.parseDouble(args[4]);


        int sim = Integer.parseInt(args[5]);
        l = Integer.parseInt(args[6]);
        end = Integer.parseInt(args[7]);

        /* Stima del RoundTripTime*/
        double ertt = 2*Math.min(buf,h)*0.01;
        System.out.println(protocol+"  "+h+"  "+buf+"  "+p+"  "+G+"  "+ertt+"  "+"  "+" l:  "+l+"   "+sim);
        Runnable launch = () -> launch(h, p, buf,G , ertt, protocol, sim);
        new Thread(launch).start();
    }
}
