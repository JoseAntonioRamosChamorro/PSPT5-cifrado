package asimetrico;


import java.awt.Color;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ServidorChatAsimetrico extends JFrame
{
	private static final long serialVersionUID = 1L;
	static ServerSocket servidor;
	static final int PUERTO = 44444;
	static int CONEXIONES = 0;
	static int ACTUALES = 0;
	static int MAXIMO = 15;
	static JTextField mensaje = new JTextField("");
	static JTextField mensaje2 = new JTextField("");
	private JScrollPane scrollpane1;
	static JTextArea textarea;
	static Socket[] tabla = new Socket[MAXIMO];

	public ServidorChatAsimetrico()
	{
		// Construimos el entorno gráfico
		super(" VENTANA DEL SERVIDOR DE CHAT ");
		setLayout(null);
		getContentPane().setBackground(Color.GRAY);
		mensaje.setBounds(10, 10, 400, 30);
		add(mensaje);
		mensaje.setEditable(false);
		mensaje2.setBounds(10, 348, 400, 30);
		add(mensaje2);
		mensaje2.setEditable(false);
		textarea = new JTextArea();
		scrollpane1 = new JScrollPane(textarea);
		scrollpane1.setBounds(10, 50, 400, 300);
		add(scrollpane1);
		textarea.setEditable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
	}

	public static void main(String args[]) throws Exception
	{
		// Desde el main se inicia el servidor
		// y las variables y se prepara la pantalla
		servidor = new ServerSocket(PUERTO);
		System.out.println("Servidor iniciado...");
		ServidorChatAsimetrico pantalla = new ServidorChatAsimetrico();
		pantalla.setBounds(0, 0, 540, 450);
		pantalla.setVisible(true);
		mensaje.setText("Número de conexiones actuales: " + 0);
		// Se usa un bucle para controlar el número de conexiones.
		// Dentro del bucle el servidor espera la conexión
		// del cliente y cuando se conecta se crea un socket
		while (CONEXIONES < MAXIMO)
		{
			Socket socket;
			try
			{
				socket = servidor.accept();
			} catch (SocketException sex)
			{
				// Sale por aquí si pulsamos el botón salir
				break;
			}
			// El socket creado se añade a la tabla,
			// se incrementa el número de conexiones
			// y se lanza el hilo para gestionar los mensajes
			// del cliente que se acaba de conectar
			tabla[CONEXIONES] = socket;
			CONEXIONES++;
			ACTUALES++;
			HiloServidorAsimetrico hilo = new HiloServidorAsimetrico(socket);
			hilo.start();
		}
		// Si se alcanzan 15 conexiones o se pulsa el botón Salir,
		// el programa se sale del bucle.
		// Al pulsar Salir se cierra el ServerSocket
		// lo que provoca una excepción (SocketException)
		// en la sentencia accept(), la excepción se captura
		// y se ejecuta un break para salir del bucle
		if (!servidor.isClosed())
		{
			try
			{
				mensaje2.setForeground(Color.red);
				mensaje2.setText("Máximo Nº de conexiones establecidas: " + CONEXIONES);
				servidor.close();
			} catch (IOException ex)
			{
				ex.printStackTrace();
			}
		} else
		{
			System.out.println("Servidor finalizado...");
		}
	}
}
