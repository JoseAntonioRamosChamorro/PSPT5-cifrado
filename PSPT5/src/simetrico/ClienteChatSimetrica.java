package simetrico;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ClienteChatSimetrica extends JFrame
{
	private static final long serialVersionUID = 1L;
	private Socket socket;
	DatagramSocket fentrada;
	DatagramSocket fsalida;
	DatagramPacket paqueteDatos;
	String nombre;
	static JTextField mensaje = new JTextField();
	private JScrollPane scrollpane;
	static JTextArea textarea;
	JButton btnEnviar = new JButton("Enviar");
	boolean repetir = true;
	static final int PORTCHAT = 44445;
	static final int PORTSOCKET = 44444;
	static final int PORTCLIENT = 44443;
	private static final byte[] ipAddr = new byte[]
			{ 127, 0, 0, 1 };

	public ClienteChatSimetrica(Socket socket, String nombre)
	{
		// Prepara la pantalla. Se recibe el socket creado y el nombre del cliente
		super(" Conexión del cliente chat: " + nombre);
		setLayout(null);
		getContentPane().setBackground(Color.GRAY);
		mensaje.setBounds(10, 10, 400, 30);
		add(mensaje);
		textarea = new JTextArea();
		scrollpane = new JScrollPane(textarea);
		scrollpane.setBounds(10, 50, 400, 300);
		add(scrollpane);
		btnEnviar.setBounds(420, 10, 100, 30);
		add(btnEnviar);
		textarea.setEditable(false);
		this.getRootPane().setDefaultButton(btnEnviar);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.socket = socket;
		this.nombre = nombre;
		// Se crean los flujos de entrada y salida.
		// En el flujo de salida se escribe un mensaje
		// indicando que el cliente se ha unido al Chat.
		// El HiloServidor recibe este mensaje y
		// lo reenvía a todos los clientes conectados
		try
		{
			fentrada = new DatagramSocket(PORTCLIENT);
			fsalida = new DatagramSocket();
			String texto = "SERVIDOR> Entra en el chat... " + nombre;
			//fsalida.send(encriptar(texto));
			cifrarSim(texto);
		} catch (Exception ex)
		{
			System.out.println("Error de E/S");
			ex.printStackTrace();
			System.exit(0);
		}
		btnEnviar.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String texto = nombre + "> " + mensaje.getText();
				try
				{
					mensaje.setText("");
					cifrarSim(texto);
				} catch (Exception ex)
				{
					ex.printStackTrace();
				}
				
			}
		});
		setResizable(false);
	}

	// El método main es el que lanza el cliente,
	// para ello en primer lugar se solicita el nombre o nick del
	// cliente, una vez especificado el nombre
	// se crea la conexión al servidor y se crear la pantalla del Chat(ClientChat)
	// lanzando su ejecución (ejecutar()).
	public static void main(String[] args) throws Exception
	{

		String nombre = JOptionPane.showInputDialog("Introduce tu nombre o nick:");
		Socket socket = null;
		try
		{
			socket = new Socket("127.0.0.1", PORTSOCKET);
		} catch (IOException ex)
		{
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "Imposible conectar con el servidor \n" + ex.getMessage(),
					"<<Mensaje de Error:1>>", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		if (!nombre.trim().equals(""))
		{
			ClienteChatSimetrica cliente = new ClienteChatSimetrica(socket, nombre);
			cliente.setBounds(0, 0, 540, 400);
			cliente.setVisible(true);
			cliente.ejecutar();
		} else
		{
			System.out.println("El nombre está vacío...");
		}
	}
	// Dentro del método ejecutar(), el cliente lee lo que el
	// hilo le manda (mensajes del Chat) y lo muestra en el textarea.
	// Esto se ejecuta en un bucle del que solo se sale
	// en el momento que el cliente pulse el botón Salir
	// y se modifique la variable repetir
	public void ejecutar()
	{
		while (repetir)
		{
			try
			{
				byte[] data = new byte[1024];
				DatagramPacket packet = new DatagramPacket(data, data.length);
				fentrada.receive(packet);
				String cadena = descifrarSim(packet);
				textarea.setText(cadena);
			} catch (IOException ex)
			{
				JOptionPane.showMessageDialog(null, "Imposible conectar con el servidor \n" + ex.getMessage(),
						"<<Mensaje de Error:2>>", JOptionPane.ERROR_MESSAGE);
				repetir = false;
			}
		}
		try
		{
			socket.close();
			System.exit(0);
		} catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}

	public void cifrarSim(String texto)
	{

		try
		{
			InetAddress address = InetAddress.getByAddress(ipAddr);
			byte[] plainBytes = texto.getBytes();
			byte[] keySymme =
				{ 0x74, 0x68, 0x69, 0x73, 0x49, 0x73, 0x41, 0x53, 0x65, 0x63, 0x72, 0x65, 0x74, 0x4b, 0x65, 0x79 }; // ClaveSecreta
			SecretKeySpec secretKey = new SecretKeySpec(keySymme, 0, 16, "AES");
			// Crear objeto Cipher e inicializar modo encriptación
			Cipher cipher = Cipher.getInstance("AES"); // Transformación
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			byte[] EncryptedData = cipher.doFinal(plainBytes);
			paqueteDatos = new DatagramPacket(EncryptedData, EncryptedData.length, address, PORTCHAT);
			System.out.println( "Mensaje cifrado por el puerto: "+ PORTCLIENT+"\t"+ new String(paqueteDatos.getData()));
			System.out.println("");
			fsalida.send(paqueteDatos);
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public String descifrarSim(DatagramPacket paquete)
	{

		String mensaje = "";

		byte[] keySymme =
			{ 0x74, 0x68, 0x69, 0x73, 0x49, 0x73, 0x41, 0x53, 0x65, 0x63, 0x72, 0x65, 0x74, 0x4b, 0x65, 0x79 }; // ClaveSecreta
		SecretKeySpec secretKey = new SecretKeySpec(keySymme, 0, 16, "AES");
		try
		{
			Cipher cipher = Cipher.getInstance("AES");
			// Reiniciar Cipher al modo desencriptado
			cipher.init(Cipher.DECRYPT_MODE, secretKey, cipher.getParameters());
			byte[] plainBytesDecrypted = cipher.doFinal(paquete.getData(), paquete.getOffset(), paquete.getLength());
			mensaje = new String(plainBytesDecrypted);
			System.out.println( "Mensaje que entra en el método descifrarSim() por el puerto: "+ PORTCLIENT+"\t"+ new String(paquete.getData()));
			System.out.println("");
			System.out.println("Mensaje desencriptado método desencriptarAsim(): " + mensaje);
			System.out.println("");
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		return mensaje;
	}
}