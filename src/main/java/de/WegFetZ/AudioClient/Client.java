package main.java.de.WegFetZ.AudioClient;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.io.*;
import java.net.*;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import main.java.de.WegFetZ.AudioClient.Utils.JTextAreaOutputStream;
import main.java.de.WegFetZ.AudioClient.gui.guiAction;
import main.java.de.WegFetZ.AudioClient.gui.mainWindow;

public class Client {

	public static final String version = "0.9";
	static String url = "http://dl.dropbox.com/u/24458406/CM_AudioClient_v";

	public static Socket cSocket = null;
	public static PrintWriter output = null;
	public static OutputStream out = null;
	public static InputStream in = null;
	public static BufferedReader input = null;
	
	public static String playername;
	public static String ServerIP;
	public static String PluginPort;
	
	
	public static boolean noReconnect = false;
	public static boolean exit = false;

	public static void main(String[] args) throws IOException {
				
		int x = 10;
		int y = 10;
		
		try {
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			x = (dim.width/2)-426;
			y = (dim.height/2)-242;	
		} catch (HeadlessException e) {}
		
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}
		mainWindow mainWindow = new mainWindow();
		mainWindow.setTitle("CustomMusic AudioClient v. " + version);
		mainWindow.setBounds(x, y, 770, 492);
		
		JTextAreaOutputStream taos;
		try {
			taos = new JTextAreaOutputStream( main.java.de.WegFetZ.AudioClient.gui.mainWindow.ta_sysOut, main.java.de.WegFetZ.AudioClient.gui.mainWindow.scroll_sysOut, 60 );
        PrintStream ps = new PrintStream( taos );
        System.setOut( ps );
        System.setErr( ps );
		} catch (Exception e) {
			System.out.println("Couldn't redirect System.out to GUI");
		}
        //redirect System.out to textarea ta_sysOut
		
		System.out.println("CustomMusic AudioClient v" + version + " started!");
		
		LoadSettings.loadMain();
		
		mainWindow.setVisible(true);
		
		if (main.java.de.WegFetZ.AudioClient.gui.mainWindow.check_debug.isSelected())
			System.out.println("INFO: Running in debug mode.");
		
		if (main.java.de.WegFetZ.AudioClient.gui.mainWindow.check_startCon.isSelected())
			new guiAction("b_connect").start();

				
	}
		

	public static void connect(String name, String ip, String port) {
		noReconnect = false;
		
		playername = name;
		ServerIP = ip;
		PluginPort = port;
		
		new File("Music/craftplayer{name=" + playername.toLowerCase() + "}/webradio/").mkdirs(); // create the Music folder and the players directory
		
		try {
			cSocket = new Socket(ip, Integer.parseInt(port));

			out = cSocket.getOutputStream();
			in = cSocket.getInputStream();
			output = new PrintWriter(cSocket.getOutputStream(), true);
			input = new BufferedReader(new InputStreamReader(cSocket.getInputStream()));
			// ==> the I/O streams

		} catch (UnknownHostException e) {
			System.err.println("Cannot connect to " + ip);
			if (mainWindow.check_debug.isSelected()) 
				e.printStackTrace();
			noReconnect = true;
			closeCons();
			return;
		
		} catch (NumberFormatException e) {
			System.err.println("Invalid port!");
			if (mainWindow.check_debug.isSelected()) 
				e.printStackTrace();
			noReconnect = true;
			closeCons();
			return;
			
		} catch (IOException e) {
			System.err.println("Cannot get the I/O for " + ip);
			System.err.println("Make sure that you have set the right port and that the ports are forwarded on the Server. \n And that the server is started of course.");
			if (mainWindow.check_debug.isSelected()) 
				e.printStackTrace();
			noReconnect = true;
			closeCons();
			return;// ==> try to connect to the CustomMusic plugin MultiServer thread

		}


		int fromServer = -1;
		try {
			if (!cSocket.isClosed()) {
				in.read();
				out.write(1);
				out.flush();
				String serverVersion = input.readLine();
				if (serverVersion != null && !serverVersion.equalsIgnoreCase(version)) {
					noReconnect = true;
					int n = JOptionPane.showOptionDialog(null, "Your AudioClient version is not supported on this server.\n" + "Do you want to download the required version?\n ", "CM_AudioClient required version: " + serverVersion, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

					if (n == JOptionPane.YES_OPTION) {
						
						BufferedInputStream datain = null;
				        FileOutputStream fout = null;
				        
				        File file = new File("CM_AudioClient_v" + serverVersion + ".jar...");
				        if (file.exists())
				        	file.delete();
				        
				        try
				        {
				                System.out.println("Downloading CM_AudioClient_v" + serverVersion + ".jar...");
				        		datain = new BufferedInputStream(new URL(url + serverVersion + ".jar").openStream());
				                fout = new FileOutputStream("CM_AudioClient_v" + serverVersion + ".jar");

				                byte data[] = new byte[4096];
				                int count;
				                
				                while ((count = datain.read(data, 0, 4096)) != -1)
				                {
				                        fout.write(data, 0, count);
				                }
				                
				                datain.close();
				                fout.close();
				                System.out.println("Download done!");
				        } catch (IOException e) {
				        	if (mainWindow.check_debug.isSelected())
				        		e.printStackTrace();
				        }
						
						Runtime.getRuntime().exec("java -jar CM_AudioClient_v" + serverVersion + ".jar ");
				        Thread.sleep(1000);
				        
						System.exit(0);

					} else {
						if (mainWindow.check_saveOnQuit.isSelected())
							LoadSettings.saveMain();
						System.out.println("Bye!");
						Thread.sleep(3000);
						System.exit(0);
					}
				} else {
					while (!exit && !cSocket.isClosed()) {
						fromServer = Integer.parseInt(input.readLine());
						try {
							if (fromServer == 1) {
								out.write(2);
								out.flush();
								System.out.println("Connected!");
							} else if (fromServer == 2) {
								output.println(playername);
							} else if (fromServer == 3) {
								System.out.println(playername + " registered! Log into Minecraft within 60s.");
								System.out.println("Use '/cm init' if the audioclient doesn't initialize automatically.");
							} else if (fromServer == -3) {
								System.out.println("Invalid Name!");
								noReconnect = true;
								exit = true;
								break;
							} else if (fromServer == 4) {
								System.out.println("AudioClient initialized!");

								// get songs from server
								Thread songReceiver = new songReceiver();
								songReceiver.start();

								// start listening for boxes and areas in range
								Thread cplaying = new CPlaying(cSocket);
								cplaying.setPriority(7);
								cplaying.start();
								
								otherFuncs.upload();

								out.write(9);
								break;
							} else if (fromServer == 5) {
								System.out.println("You don't have permission to use the AudioClient!");
								noReconnect = true;
								exit = true;
								break;
							} else if (fromServer == 6) {
								System.out.println("Server couldn't verify your IP. Make sure you entered the right name.");
								noReconnect = true;
								exit = true;
								break;
							} else if (fromServer == 7) {
								System.out.println("Server couldn't verify your ingame Player. Make sure you entered the right name.");
								noReconnect = true;
								exit = true;
								break;
							} else if (fromServer == -4) {
								System.out.println("Initialization failed!");
								noReconnect = true;
								exit = true;
								break;
							} else if (fromServer == -5) {
								System.out.println("Server reported an error.");
								exit = true;
								break;
							} else if (fromServer == -1) {
								System.out.println("Connection lost.");
								exit = true;
								break;
							} else { 
								System.out.println("Invalid reply from Server: " + fromServer);
								exit = true;
								break;
							}
						} catch (IOException e) {
							if (mainWindow.check_debug.isSelected()) e.printStackTrace();
							System.out.println("Connection lost");
							exit = true;
							break;
						}
					}
				}
			}

			if (exit || cSocket.isClosed() || fromServer == -1) {
				closeCons();
			}
			
		} catch (IOException e) {
			if (mainWindow.check_debug.isSelected()) e.printStackTrace();
			closeCons();
		} catch (InterruptedException e) {
			if (mainWindow.check_debug.isSelected()) e.printStackTrace();
		} catch (NumberFormatException e) {
			if (mainWindow.check_debug.isSelected()) e.printStackTrace();
			System.out.println("Unsupported answer from server.");
			closeCons();
		} catch (NullPointerException e) {
			if (mainWindow.check_debug.isSelected()) e.printStackTrace();
			System.out.println("Couldn't read inputstream.");
			closeCons();
		}


	}

	public static void closeCons() {
		
		try {
			if (cSocket != null && !cSocket.isClosed()) {
				out.write(0);
				out.flush();

				cSocket.close();
			}
		} catch (IOException e) {
			//nothing
		}
		exit = true;

		if (mainWindow.b_connect.isEnabled()) {
			mainWindow.b_connect.setText("Connect");
			mainWindow.b_connect.setEnabled(false);
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {}
			
				System.out.println("Connections closed.");	
	
			if (mainWindow.check_reconnect.isSelected())
				reconnect();
			mainWindow.b_connect.setEnabled(true);
		}
	}


	private static void reconnect() {
		if (playername.length() > 0 && ServerIP.length() > 0 && PluginPort.length() > 0) {
			if (!noReconnect && mainWindow.b_connect.getText().equalsIgnoreCase("Connect")) {
				
				exit = false;
				mainWindow.b_connect.setText("Disconnect");
				System.out.println();
				mainWindow.b_connect.setEnabled(true);
				connect(playername, ServerIP, PluginPort);
			} else if(noReconnect) {
				System.out.println("Client will not reconnect.");
			}
		}
		
	}

}
