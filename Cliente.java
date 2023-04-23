import java.io.*;
import java.lang.management.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;

public class Cliente {

    public static void main(String[] args) {
        final var enderecoIP = "192.168.0.35"; // Altere com o endereço IP do servidor desejado
        final var thread = new Thread(new ServidorHandler(enderecoIP));
        thread.start();
    }
}

class ServidorHandler implements Runnable {
    private final String enderecoIP;

    public ServidorHandler(final String enderecoIP) {
        this.enderecoIP = enderecoIP;
    }

    @Override
    public void run() {
        try {
            final var timer = new Timer();
            timer.scheduleAtFixedRate(new RecursoSender(enderecoIP), 0, 10000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class RecursoSender extends TimerTask {
    private final String enderecoIP;

    public RecursoSender(final String enderecoIP) {
        this.enderecoIP = enderecoIP;
    }

    @Override
    public void run() {
        Socket socket = null;
        try {
            socket = new Socket(enderecoIP, 12345);
            System.out.println("Conectado ao servidor: " + enderecoIP);

            final var recursos = new HashMap<>();
            final var osBean = ManagementFactory.getOperatingSystemMXBean();
            recursos.put("Sistema Operacional", osBean.getName());
            recursos.put("Arquitetura", osBean.getArch());
            recursos.put("Versão", osBean.getVersion());
            recursos.put("Memória Livre", String.valueOf(Runtime.getRuntime().freeMemory()));
            recursos.put("Memória Em Uso", String.valueOf(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
            recursos.put("Modelo do Processador", System.getProperty("os.arch"));
            recursos.put("Carga do Processador", String.valueOf(Runtime.getRuntime().availableProcessors()));
            recursos.put("Armazenamento", getArmazenamento());
            recursos.put("Rede", getEnderecoIP());

            final var objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(recursos);
            objectOutputStream.flush();
            System.out.println("Recursos enviados para o servidor - " + LocalDateTime.now());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getArmazenamento() {
        try {
            final var process = Runtime.getRuntime().exec("wmic logicaldisk get DeviceID, FreeSpace, Size");
            final var bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            final var sb = new StringBuilder();

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
            }

            bufferedReader.close();
            process.waitFor();

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Informação de armazenamento não disponível";
    }

    private String getEnderecoIP() {
        try {
            final var address = InetAddress.getLocalHost();
            final var networkInterface = NetworkInterface.getByInetAddress(address);
            final var mac = networkInterface.getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Informação de rede não disponível";
    }
}
