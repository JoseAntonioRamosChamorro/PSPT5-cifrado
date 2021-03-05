package asimetrico;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ClienteChatAsimetrico extends JFrame{
	private static final long serialVersionUID = 1L;
	Socket socket;
	DataInputStream fentrada;
	static DataOutputStream fsalida;
	String nombre;
	static JTextField mensaje = new JTextField();
	private JScrollPane scrollpane;
	static JTextArea textarea;
	JButton btnEnviar = new JButton("Enviar");
	boolean repetir = true;
	static final String HOST = "127.0.0.1";
	static final int PUERTO = 44444;
	Vector<String> lineas = new Vector<String>();
	String aux;

	public ClienteChatAsimetrico(Socket socket, String nombre)

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
			fentrada = new DataInputStream(socket.getInputStream());
			fsalida = new DataOutputStream(socket.getOutputStream());
			String texto = "SERVIDOR> Entra en el chat... " + nombre;
			// Escritura de texto en el DataOutputStream
			encriptarAsim(socket, texto);
		} catch (IOException ex)
		{
			System.out.println("Error de E/S");
			ex.printStackTrace();
			System.exit(0);
		}
		setResizable(false);
		// Cuando se pulsa el botón Enviar el mensaje introducido se envía al servidor por el flujo de salida
		btnEnviar.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String texto = nombre + "> " + mensaje.getText();
				mensaje.setText("");
				// Escritura de texto en el DataOutputStream
				encriptarAsim(socket, texto);
				lineas.removeAllElements();

			}
		});
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
			socket = new Socket(HOST, PUERTO);
		} catch (IOException ex)
		{
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "Imposible conectar con el servidor \n" + ex.getMessage(),
					"<<Mensaje de Error:1>>", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		if (!nombre.trim().equals(""))
		{
			ClienteChatAsimetrico cliente = new ClienteChatAsimetrico(socket, nombre);
			cliente.setBounds(0, 0, 540, 400);
			cliente.setVisible(true);
			cliente.ejecutar();
		} else
		{
			System.out.println("El nombre está vacío...");
		}
	}

	public void ejecutar()
	{
		String texto = "";
		while (repetir)
		{
			for (int i = 0; i < lineas.size(); i++)
			{
				String linea = String.valueOf(lineas.elementAt(i));
				textarea.append(linea + "\n");
			}

			try
			{
				texto = fentrada.readUTF();
				aux = desencriptarAsim(texto);
				lineas.add(aux);
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

	// Devuelve un flujoSalida.writeUTF()
	public static DataOutputStream encriptarAsim(Socket cliente, String mensaje)
	{

		try
		{
			fsalida = new DataOutputStream(cliente.getOutputStream());
			// Trabajamos con las claves privadas y públicas
			RSA rsa = new RSA();
			rsa.genKeyPair(512);
			rsa.saveToDiskPrivateKey("rsa.pri");
			rsa.saveToDiskPublicKey("rsa.pub");
			// Ciframos e imprimimos, el texto cifrado es devuelto en la variable secure
			String secure = rsa.Encrypt(mensaje);
			fsalida.writeUTF(secure);

			System.out.println("Mensaje a encriptar: " + mensaje);
			System.out.println("");
			System.out.println("Mensaje encriptado con encriptarAsim(): " + secure);
			System.out.println("");
		} catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
		// Devuelve un objeto flujoSalida.writeUTF (pero encriptado)
		return fsalida;
	}

	public String desencriptarAsim(String mensaje)
	{
		String decrypt = "";
		// Trabajamos con las claves privadas y públicas
		try
		{
			RSA rsaCliente = new RSA();
			rsaCliente.genKeyPair(512);
			rsaCliente.openFromDiskPrivateKey("rsaCliente.pri");
			rsaCliente.openFromDiskPublicKey("rsaCliente.pub");
			decrypt = rsaCliente.Decrypt(mensaje);
			System.out.println("Mensaje a desencriptar: " + mensaje);
			System.out.println("");
			System.out.println("Mensaje desencriptado método desencriptarAsim(): " + decrypt);
			System.out.println("");

		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return decrypt;
	}
}