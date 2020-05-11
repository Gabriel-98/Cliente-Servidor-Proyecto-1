import ClientServerImplementation.ConnectionManager;
import ClientServerImplementation.Service;
import ClientServerImplementation.ClientInformation;
import ClientServerImplementation.ServiceConfiguration;
import ServerManager.CentralServerManager;
import Services.CentralServices.ClientRegisterService;
import Services.CentralServices.ClientLoginService;
import Services.CentralServices.ClientLogoutService;
import Services.CentralServices.FileRegisterService;
import Services.CentralServices.FileRemoveService;
import Services.CentralServices.FileSearchService;
import Services.CentralServices.ServerNodeSearchService;
import Services.CentralServices.IntermediaryServerSearchService;
import Services.CentralServices.IntermediaryRegisterService;
import Services.CentralServices.ConnectionRegisterService;
import Services.CentralServices.ConnectionRemoveService;
import Services.CentralServices.IntermediaryRemoveService;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.Vector;


public class CentralServer{
	public static void main(String[] args){
		CentralServerManager centralServerManager = new CentralServerManager();

		ConnectionManager connectionManagerToIntermediaries;
		ConnectionManager connectionManagerToClients;
		try{
			connectionManagerToIntermediaries = new ConnectionManager("", 11000, 1024);
			connectionManagerToClients = new ConnectionManager("central-to-clients", 10000, 1024);
		}
		catch(Exception e){
			System.out.println("No se pudo crear el servidor");
			return;
		}

		// servicios que se ejecutan al desconectarse un cliente o un servidor intermediario
		connectionManagerToIntermediaries.setDisconnectService(new IntermediaryRemoveService(centralServerManager));
		connectionManagerToClients.setDisconnectService(new ClientLogoutService(centralServerManager));
		
		// adicionar servicios
		connectionManagerToIntermediaries.addService(new IntermediaryRegisterService(centralServerManager));
		connectionManagerToIntermediaries.addService(new ConnectionRegisterService(centralServerManager));
		connectionManagerToIntermediaries.addService(new ConnectionRemoveService(centralServerManager));
		connectionManagerToClients.addService(new ClientRegisterService(centralServerManager));
		connectionManagerToClients.addService(new ClientLoginService(centralServerManager));
		connectionManagerToClients.addService(new FileRegisterService(centralServerManager));
		connectionManagerToClients.addService(new FileRemoveService(centralServerManager));
		connectionManagerToClients.addService(new FileSearchService(centralServerManager));
		connectionManagerToClients.addService(new ServerNodeSearchService(centralServerManager));
		connectionManagerToClients.addService(new IntermediaryServerSearchService(centralServerManager));	

		// adicionar configuracion servicios
		connectionManagerToClients.addServiceConfiguration(new ServiceConfiguration("registrar-cliente", true, true));
		connectionManagerToClients.addServiceConfiguration(new ServiceConfiguration("respuesta-registrar-cliente", true, true));

		connectionManagerToClients.setShowMessages(false);
		connectionManagerToIntermediaries.setShowMessages(false);

		ExecutorService hilos = Executors.newFixedThreadPool(3);
		hilos.execute(centralServerManager);
		hilos.execute(connectionManagerToIntermediaries);
		hilos.execute(connectionManagerToClients);		
	}
}
