package simetrico;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
public class HiloServidorSimetrica extends Thread
{
	DatagramSocket fentrada;
	DatagramSocket fsalida;
	DatagramPacket paqueteDatos;
	Socket socket;
	boolean fin = false;
	static final int PORTCHAT = 44445;
	static final int PORTSOCKET = 44444;
	static final int PORTCLIENT = 44443;
	private static final byte[] ipAddr = new byte[]
			{ 127, 0, 0, 1 };
	public HiloServidorSimetrica(Socket socket)
	{
		this.socket = socket;
		try
		{
			fentrada = new DatagramSocket(PORTCHAT);
			fsalida = new DatagramSocket();
		}
		catch (IOException e)
		{
			System.out.println("Error de E/S");
			e.printStackTrace();
		}
	}
	// En el método run() lo primero que hacemos
	// es enviar todos los mensajes actuales al cliente que se
	// acaba de incorporar
	public synchronized void run()
	{
		ServidorChatSimetrica.mensaje.setText("Número de conexiones actuales: " +
				ServidorChatSimetrica.ACTUALES);
		String texto = ServidorChatSimetrica.textarea.getText();
		EnviarMensajes(texto);
		// Seguidamente, se crea un bucle en el que se recibe lo que el cliente escribe en el chat.
		// Cuando un cliente finaliza con el botón Salir, se envía un * al servidor del Chat,
		// entonces se sale del bucle while, ya que termina el proceso del cliente,
		// de esta manera se controlan las conexiones actuales
		while(!fin)
		{
			try
			{				
				byte[] data = new byte[1024];
				paqueteDatos = new DatagramPacket(data, data.length);
				fentrada.receive(paqueteDatos);
				String desencriptar = descifrarSim(paqueteDatos);
				if(desencriptar.trim().equals("*"))
				{
					ServidorChatSimetrica.ACTUALES--;
					ServidorChatSimetrica.mensaje.setText("Número de conexiones actuales: "
							+ ServidorChatSimetrica.ACTUALES);
					fin=true;
				}
				// El texto que el cliente escribe en el chat,
				// se añade al textarea del servidor y se reenvía a todos los clientes
				else
				{
					ServidorChatSimetrica.textarea.append(desencriptar + "\n");
					texto = ServidorChatSimetrica.textarea.getText();
					
					EnviarMensajes(texto);
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				fin=true;
			}
		}
	}
	// El método EnviarMensajes() envía el texto del textarea a
	// todos los sockets que están en la tabla de sockets,
	// de esta forma todos ven la conversación.
	// El programa abre un stream de salida para escribir el texto en el socket

	private void EnviarMensajes(String texto)
	{
		for(int i=0; i<ServidorChatSimetrica.CONEXIONES; i++)
		{
			socket = ServidorChatSimetrica.tabla[i];
			try
			{
				cifrarSim(texto);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	public void cifrarSim(String mensaje){
		
		try {
			InetAddress address = InetAddress.getByAddress(ipAddr);
			byte[] plainBytes = mensaje.getBytes();
			byte[] keySymme = {0x74, 0x68, 0x69, 0x73, 0x49, 0x73, 0x41, 0x53, 0x65, 0x63, 0x72, 0x65, 0x74, 0x4b, 0x65, 0x79}; // ClaveSecreta
			SecretKeySpec secretKey = new SecretKeySpec(keySymme, 0, 16, "AES");
			// Crear objeto Cipher e inicializar modo encriptación
			Cipher cipher = Cipher.getInstance("AES"); // Transformación
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			byte[] EncryptedData = cipher.doFinal(plainBytes);	
			paqueteDatos = new DatagramPacket(EncryptedData, EncryptedData.length, address, PORTCLIENT);
			fsalida.send(paqueteDatos);
			System.out.println( "Mensaje cifrado por el puerto: "+ PORTCLIENT+"\t"+ new String(paqueteDatos.getData()));
			System.out.println("");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public String descifrarSim(DatagramPacket paquete){
		
		String mensaje = "";
		
		byte[] keySymme = {0x74, 0x68, 0x69, 0x73, 0x49, 0x73, 0x41, 0x53, 0x65, 0x63, 0x72, 0x65, 0x74, 0x4b, 0x65, 0x79}; // ClaveSecreta
		SecretKeySpec secretKey = new SecretKeySpec(keySymme, 0, 16, "AES");
		try
		{
			Cipher cipher = Cipher.getInstance("AES");
			// Reiniciar Cipher al modo desencriptado
			cipher.init(Cipher.DECRYPT_MODE, secretKey, cipher.getParameters());
			byte[] plainBytesDecrypted = cipher.doFinal(paquete.getData(),paquete.getOffset(), paquete.getLength());
			mensaje = new String(plainBytesDecrypted);

			System.out.println( "Mensaje que entra en el método descifrarSim() por el puerto: "+ PORTCLIENT+"\t"+ new String(paquete.getData()));
			System.out.println("");
			System.out.println("Mensaje desencriptado método desencriptarAsim(): " + mensaje);
			System.out.println("");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return mensaje;
	}
}