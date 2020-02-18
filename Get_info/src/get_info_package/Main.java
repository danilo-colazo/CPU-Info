package get_info_package;

import java.sql.Statement;

import com.sun.jna.platform.win32.Advapi32Util;
import static com.sun.jna.platform.win32.WinReg.HKEY_LOCAL_MACHINE;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;


public class Main {
	
	//Campos de las función principal
	private static FileWriter fichero;
	private static PrintWriter pw;
	private static String[] datos;
	private static String[] datos_DB;
	private static final int TAM = 12;
	private static final int IDEX_KEY = TAM-1; 
	private static String serviceName;
	
	public static void main(String[] args) throws Exception {
		
		//Inicialización de los campos
		datos = new String[TAM];
		datos_DB = new String[TAM-1];
        fichero = null;
        pw = null;
        serviceName = "TeamViewer";

        //Se almacena en la posición cero, del array de strings datos[], el nombre del procesador.
		datos[0] = Advapi32Util.registryGetStringValue
        		(HKEY_LOCAL_MACHINE,"HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\0\\",
            		"ProcessorNameString");
		System.out.println("0_ Procesador: "+datos[0]);
		
		//Se almacena en la posición uno, del array de strings datos[], el nombre del sistema operativo.
		datos[1] = System.getProperty("os.name");
    	System.out.println("1_ Sistema operativo: "+datos[1]);
    	
    	//Se almacena en la posición dos, del array de strings datos[], la versión del sistema operativo.
    	datos[2] = System.getProperty("os.version");
    	System.out.println("2_ Versión sistema operativo: "+datos[2]);
    	
    	//Se almacena en la posición tres, del array de strings datos[], la arquitectura del sistema operativo.
    	datos[3] = System.getProperty("os.arch");
    	System.out.println("3_ Arquitectura sistema operativo: "+datos[3]);
    	
    	//Se almacena en la posición cuatro, del array de strings datos[], el tamaño del disco duro en Gb
    	//redondeado a dos cifras significativas.
    	long diskSize = new File("/").getTotalSpace();
    	datos[4]=""+round(diskSize/10e8,2)+" GB";
    	System.out.println("4_ Tamaño del Disco duro: "+round(diskSize/10e8,2)+" GB");
    	
    	//Se almacena en la posición cinco, del array de strings datos[], el tamaño de la memoria RAM en Gb
    	//redondeado a dos cifras significativas.
    	long memorySize = ((com.sun.management.OperatingSystemMXBean) ManagementFactory
    	        .getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
    	datos[5] =""+ round(memorySize/10e8,2)+" GB";
    	System.out.println("5_ Tamaño de la memoria RAM: "+round(memorySize/10e8,2)+" GB");
    	
    	//Se almacena en la posición seis, del array de strings datos[], la dirección IP de la PC.
        InetAddress ip;
    	try {
    			
    		ip = InetAddress.getLocalHost();
    		datos[6]=""+ip.getHostAddress();
    		System.out.println("6_ Dirección IP : " + datos[6]);
    		
    		NetworkInterface network = NetworkInterface.getByInetAddress(ip);
    			
    		byte[] mac = network.getHardwareAddress();
    			
    		StringBuilder sb = new StringBuilder();
    		for (int i = 0; i < mac.length; i++) {
    			sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));		
    		}
    		
    		//Se almacena en la última posición, del array de strings datos[], la dirección MAC de la PC. 
    		datos[IDEX_KEY]=""+sb.toString();
    			
    	} catch (UnknownHostException e) {
    		e.printStackTrace();
    		escribirArchivo("UnknownHostExceptionIP_",datos[8], "catch UnknownHostException IP address. " + e.getMessage());
    	} catch (SocketException e) {
    		e.printStackTrace();
    		escribirArchivo("SocketException_",datos[8], "catch SocketException. "+e.getMessage());
    	}
    	
    	//Se almacena en la posición siete, del array de strings datos[], el nombre de usuario que inició sesión
    	datos[7]=""+System.getProperty("user.name");
    	System.out.println("7_ Nomnre de usuario: "+datos[7]);
    	
    	//Se almacena en la posición ocho, del array de strings datos[], el nombre del equipo.
		try {
		    InetAddress addr;
		    addr = InetAddress.getLocalHost();
		    datos[8] = addr.getHostName();
		    System.out.println("8_ Nombre_equipo: " + datos[8]);
		} catch (UnknownHostException ex) {
		    System.out.println("Hostname can not be resolved");
		    escribirArchivo("UnknownHostExceptionLocalHost_",datos[8], "Hostname can not be resolved. "+ex.getMessage());
		}
		
		//Se prueba si el equipo tiene conexión a internet.
		//Si tiene conexión se almacena en la posición nueve, del array de strings datos[], el string "Conectado".
		//Si NO tiene conexión se almacena en la posición nueve, del array de strings datos[], el string "Desconectado".
		try {
			URL url = new URL("http://www.google.com");
			URLConnection connection = url.openConnection();
			connection.connect();
			datos[9] = "Conectado";
			System.out.println("9_ Internet: " + datos[9]);
		} catch (MalformedURLException e) {
			datos[9] = "Desconectado";
			System.out.println("MalformedURLException - 9_ Internet: " + datos[9]);
		} catch (IOException e) {
			datos[9] = "Desconectado";
			System.out.println("IOException - 9_ Internet: " + datos[9]);
		}
		
		//Se prueba si se encuentra instalado en la PC el software Teamviewer.
		//Si está instalado se almacena en la posición diez, del array de strings datos[], el string "SI".
		//Si NO está instalado se almacena en la posición diez, del array de strings datos[], el string "NO".
		String s = runProcess("sc query "+serviceName+" | findstr RUNNING");
		if(s.contains("RUNNING")) {
			datos[10] = "SI";
			System.out.println("10_Tiene TeamViewer: SI");
		}
		else {
			datos[10] = "NO";
			System.out.println("10_Tiene TeamViewer: NO");
		}
		
		System.out.print("11_ Dirección MAC : ");
		System.out.println(datos[IDEX_KEY]+"\n\n\n");
				
		try {
			//Se carga el driver para realizar la conexión a la BDD.
			Class.forName("com.mysql.cj.jdbc.Driver");
			
			//Se crea un objeto del tipo Connection, para administrar la conexión.
			//Se utiliza la siguiente sintaxis: jdbc:subprotocolo//servidor:puerto/base de datos,user,pass
	    	Connection con = DriverManager.getConnection("jdbc:mysql://ip_servidor:puerto/base de datos","user","pass");
			
	    	//Se crea una sentencia para reliazar una consulta a la BDD.
			PreparedStatement statement = con.prepareStatement("SELECT Procesador,Nombre_SO,Version_SO,Arquitectura_SO,"
					+ "TAM_disco,TAM_RAM,Dir_IP,Usuario,Nombre_equipo,Internet,TeamViewer FROM CPU_info.tabla_pcInfo WHERE Dir_MAC="+"\""+datos[IDEX_KEY]+"\"");
	
			//Se ejecuta la consulta y se guarda lo obtenido en una variable de tipo ResultSet.
			ResultSet result = statement.executeQuery();
			
			//Se verifica si ya existe la información de la PC en la BDD.
			if(result.next()) {
				//Si existe se ejecuta nuevamente la consulta
				result = statement.executeQuery();
				while(result.next()) {
					//Se almacenan los datos de la consulta en un array de string
					for(int i=1; i<datos.length; i++) {
						datos_DB[i-1]=result.getString(i);
					}
				}
				//Se cierra la conexión.
				con.close();
				
				for(int i=0; i<datos_DB.length; i++) {
					System.out.println(" "+datos_DB[i]+" ---- "+datos[i]);
				}
				
				//En el siguiente for se compara cada valor de la BDD con los datos nuevos.
				//Si hay algún dato distinto se actualiza la BDD con el dato nuevo.
				String c_name="";
				for(int i=0; i<datos_DB.length; i++) {
					System.out.println("COLUMNA["+i+"]: "+datos_DB[i]);
					switch (i) {
						case 0: {
							c_name="Procesador";
							if(datos_DB[i]!=null) {
								if(!datos_DB[i].equals(datos[i])) {
									actualizarFila(c_name,i);
								}
							}else {
								actualizarFila(c_name,i);
							}
							break;
						}
						case 1: {
							c_name="Nombre_SO";
							if(datos_DB[i]!=null) {
								if(!datos_DB[i].equals(datos[i])) {
									actualizarFila(c_name,i);
								}
							}else {
								actualizarFila(c_name,i);
							}
							break;
						}
						case 2: {
							c_name="Version_SO";
							if(datos_DB[i]!=null) {
								if(!datos_DB[i].equals(datos[i])) {
									actualizarFila(c_name,i);
								}
							}else {
								actualizarFila(c_name,i);
							}
							break;
						}
						case 3: {
							c_name="Arquitectura_SO";
							if(datos_DB[i]!=null) {
								if(!datos_DB[i].equals(datos[i])) {
									actualizarFila(c_name,i);
								}
							}else {
								actualizarFila(c_name,i);
							}
							break;
						}
						case 4: {
							c_name="TAM_disco";
							if(datos_DB[i]!=null) {
								if(!datos_DB[i].equals(datos[i])) {
									actualizarFila(c_name,i);
								}
							}else {
								actualizarFila(c_name,i);
							}
							break;
						}
						case 5: {
							c_name="TAM_RAM";
							if(datos_DB[i]!=null) {
								if(!datos_DB[i].equals(datos[i])) {
									actualizarFila(c_name,i);
								}
							}else {
								actualizarFila(c_name,i);
							}
							break;
						}
						case 6: {
							c_name="Dir_IP";
							if(datos_DB[i]!=null) {
								if(!datos_DB[i].equals(datos[i])) {
									actualizarFila(c_name,i);
								}
							}else {
								actualizarFila(c_name,i);
							}
							break;
						}
						case 7: {
							c_name="Usuario";
							if(datos_DB[i]!=null) {
								if(!datos_DB[i].equals(datos[i])) {
									actualizarFila(c_name,i);
								}
							}else {
								actualizarFila(c_name,i);
							}
							break;
						}
						case 8: {
							c_name="Nombre_equipo";
							if(datos_DB[i]!=null) {
								if(!datos_DB[i].equals(datos[i])) {
									actualizarFila(c_name,i);
								}
							}else {
								actualizarFila(c_name,i);
							}
							break;
						}
						case 9: {
							c_name="Internet";
							if(datos_DB[i]!=null) {
								if(!datos_DB[i].equals(datos[i])) {
									actualizarFila(c_name,i);
								}
							}else {
								actualizarFila(c_name,i);
							}
							break;
						}
						case 10: {
							c_name="TeamViewer";
							if(datos_DB[i]!=null) {
								if(!datos_DB[i].equals(datos[i])) {
									actualizarFila(c_name,i);
								}
							}else {
								actualizarFila(c_name,i);
							}
							break;
						}
						default:
							break;
					}
				}
			} 
			//En caso de que no existan los datos de la PC en la BDD se crea una sentencia para su inserción.
			else {
				System.out.println("\n\nLa tabla está vacia");
				String query = "INSERT INTO CPU_info.tabla_pcInfo(Procesador,Nombre_SO,Version_SO,Arquitectura_SO,"
						+ "TAM_disco,TAM_RAM,Dir_IP,Usuario,Nombre_equipo,Internet,TeamViewer,Dir_MAC) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
				System.out.println(query);
				
				PreparedStatement InsertStatement = con.prepareStatement(query);
				for(int i=0; i<datos.length; i++) {
					InsertStatement.setString (i+1, datos[i]);
				}
				InsertStatement.execute();
			}
			
			//Para finalizar se escribe un archivo para indicar que la ejecución del programa se llevó a cabo con éxito.
			escribirArchivo("respuesta_",datos[8], "¡Ejecución exitosa!");
			
		}catch (Exception e) {
		      System.err.println("Got an exception!");
		      System.err.println(e.getMessage());
		      escribirArchivo("Exception_",datos[8], "catch Exception. "+e.getMessage());
		}
	}
	
	/*
	 * Función encargada de redondear los valores decimales a tantas cifras significativas como sea indicado.
	 * @Param value es el valor tipo double que se desea redondear.
	 * @Param places son las cifras significativas que tendrá el valor.
	 *   
	 * */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
    
    /*
     * Función encargada de actualizar los datos en la BDD cuando se produce algún cambio en la PC.
     * Ej. Se cambia el Disco duro por otro de diferente tamaño, Se cambia el nombre del equipo, etc,
     * @Param nombre_c es el nombre de la columna que se debe modificar.
     * @Param i es el índice de la lista de datos.
     * @Return void.
     * 
     * */
    public static void actualizarFila(String nombre_c, int i) {	
		try {
			Connection conn = DriverManager.getConnection("jdbc:mysql://ip_servidor:puerto/base de datos","user","pass");
			Statement stmt = conn.createStatement();
			stmt.executeUpdate("update CPU_info.tabla_pcInfo set "+nombre_c+" = "+"\""+datos[i]+"\""+" where Dir_MAC="+"\""+datos[IDEX_KEY]+"\"");
			System.out.println("update CPU_info.tabla_pcInfo set "+nombre_c+" = "+"\""+datos[i]+"\""+" where Dir_MAC="+"\""+datos[IDEX_KEY]+"\"");
			conn.close();
		}catch(SQLException e) {
			e.printStackTrace();
			escribirArchivo("SQLException_",datos[8], "catch SQLException. "+e.getMessage());
		}
	}
    
    /*
     * Función encargada de crear y escribir un archivo con los parámetros correspondientes.
     * @Param filename es el nombre del archivo.
     * @Param equipo es el nombre de la PC que va asociado al nombre del archivo a modo de identificación.
     * @Param linea es el contenido que será escrito en el archivo.
     * @Return void.
     *  
     * */
    public static void escribirArchivo(String fileName, String equipo ,String linea) {
        try
        {
        	fichero = new FileWriter(fileName+equipo+".txt");
            pw = new PrintWriter(fichero);
            pw.println(linea);
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
           try {
           // La cláusula finally se utiliza para 
           // asegurarse que se cierra el fichero.
           if (null != fichero)
              fichero.close();
           } catch (Exception e2) {
              e2.printStackTrace();
           }
        }
    }
    
    /*
     * Función que se encarga de ejecutar un comando en la consola de windows cmd.
     * @Param string es la linea de comando que se ejecuta en la consola cmd.
     * @Return String es el resultado devuelto de la ejecución del comando.
     * 
     * */
	private static String runProcess(String string) {
		// Se crea el proceso. 
		// "cmd.exe" es el nombre de la consola que ejecutará el comando.
		// "/c" indica que se ejecuta el comando especificado por la cadena y luego termina.
		// string es la linea de comando a ejecutar.
        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", string);
        builder.redirectErrorStream(true);
        Process p = null;
        // Se incia el proceso.
        try {
        	p = builder.start();
        } catch (IOException e) {
        	e.printStackTrace();
        }
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
	    String line = "";
	    StringBuilder stringBuilder = new StringBuilder();
	    //Se obtiene el resultado devuelto de la ejecución del comando.
	    while (true) {
	    	try {
	    		line = r.readLine();

				if (line == null) { break; }
				stringBuilder.append(line);
			} catch (IOException e) {
					e.printStackTrace();
			}
	   }
	   return stringBuilder.toString();
	}
}