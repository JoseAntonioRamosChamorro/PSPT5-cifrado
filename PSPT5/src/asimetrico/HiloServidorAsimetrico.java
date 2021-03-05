package asimetrico;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Vector;

public class HiloServidorAsimetrico extends Thread
{
	static DataInputStream fentrada;
	static DataOutputStream fsalida;
	Socket socket;
	boolean fin = false;
	static String aux = "";
	Vector<String> lineas = new Vector<String>();

	public HiloServidorAsimetrico(Socket socket)
	{
		this.socket = socket;
		try
		{
			fentrada = new DataInputStream(socket.getInputStream());

		} catch (IOException e)
		{
			System.out.println("Error de E/S");
			e.printStackTrace();
		}
	}

	// En el método run() lo primero que hacemos
	// es enviar todos los mensajes actuales al cliente que se
	// acaba de incorporar
	public void run()
	{

		ServidorChatAsimetrico.mensaje.setText("Número de conexiones actuales: " + ServidorChatAsimetrico.ACTUALES);
		String texto = ServidorChatAsimetrico.textarea.getText();
		EnviarMensajes(texto);
		// Seguidamente, se crea un bucle en el que se recibe lo que el cliente escribe
		// en el chat.
		// Cuando un cliente finaliza con el botón Salir, se envía un * al servidor del
		// Chat,
		// entonces se sale del bucle while, ya que termina el proceso del cliente,
		// de esta manera se controlan las conexiones actuales
		while (!fin)
		{
			String cadena = "";
			try
			{
				// Leemos el contenido del objeto DataInputStream
				cadena = fentrada.readUTF();
				// Desencriptamos para capturar el contenido del DataInputStream descodificado
				aux = desencriptarAsim(cadena);

				if (aux.trim().equals("*"))
				{
					ServidorChatAsimetrico.ACTUALES--;
					ServidorChatAsimetrico.mensaje
							.setText("Número de conexiones actuales: " + ServidorChatAsimetrico.ACTUALES);
					fin = true;
				}
				// El texto que el cliente escribe en el chat,
				// se añade al textarea del servidor y se reenvía a todos los clientes
				else
				{
					ServidorChatAsimetrico.textarea.append(aux + "\n");
					String[] linea = ServidorChatAsimetrico.textarea.getText().split("\n");

					for (int i = 0; i < linea.length; i++)
					{
						String auxiliar = linea[i];
						lineas.add(auxiliar);

					}
					if (lineas.lastElement() != "")
					{

						EnviarMensajes(lineas.lastElement());
					}

				}
			} catch (Exception ex)
			{
				ex.printStackTrace();
				fin = true;
			}
		}
	}

	// El método EnviarMensajes() envía el texto del textarea a
	// todos los sockets que están en la tabla de sockets,
	// de esta forma todos ven la conversación.
	// El programa abre un stream de salida para escribir el texto en el socket
	private void EnviarMensajes(String texto)
	{
		for (int i = 0; i < ServidorChatAsimetrico.CONEXIONES; i++)
		{
			Socket socket = ServidorChatAsimetrico.tabla[i];
			// Escritura de texto en el DataOutputStream
			encriptarAsim(socket, texto);
		}
	}

	public String desencriptarAsim(String mensaje)
	{

		String decrypt = "";
		try
		{

			System.out.println("Esperando al cliente...");
			RSA rsa = new RSA();
			rsa.genKeyPair(512);
			rsa.openFromDiskPrivateKey("rsa.pri");
			rsa.openFromDiskPublicKey("rsa.pub");
			decrypt = rsa.Decrypt(mensaje);
			System.out.println("Mensaje a desencriptar: " + mensaje);
			System.out.println("");
			System.out.println("Mensaje desencriptado método desencriptarAsim(): " + decrypt);
			System.out.println("");
		} catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
		return decrypt;
	}

	// Devuelve un objeto DataOuputStream flujoSalida.writeUTF()
	public static DataOutputStream encriptarAsim(Socket cliente, String mensaje)
	{

		try
		{

			fsalida = new DataOutputStream(cliente.getOutputStream());
			// Trabajamos con las claves privadas y públicas
			RSA rsa = new RSA();
			rsa.genKeyPair(512);
			rsa.saveToDiskPrivateKey("rsaCliente.pri");
			rsa.saveToDiskPublicKey("rsaCliente.pub");
			// Ciframos e imprimimos, el texto cifrado es devuelto en la variable secure
			String secure = rsa.Encrypt(mensaje);
			System.out.println("Mensaje a encriptar: " + mensaje);
			System.out.println("");
			System.out.println("Mensaje encriptado con encriptarAsim(): " + secure);
			System.out.println("");
			fsalida.writeUTF(secure);

		} catch (Exception e)
		{
			System.out.println(e.getMessage());
		}
		// Devuelve un objeto flujoSalida.writeUTF (pero encriptado)
		return fsalida;
	}

}