
import java.io.File;
import java.util.Scanner;

import federate.RFPAmbassador;
import federate.RFPFederate;
import skf.config.Configuration;
import skf.config.ConfigurationFactory;
import skf.model.interaction.modeTransitionRequest.MTRMode;

public class Main {

	private static final File conf = new File("conf/conf.json");

	public static void main(String[] args) throws Exception {
		
		ConfigurationFactory factory = new ConfigurationFactory();
		Configuration configuration = factory.importConfiguration(conf);

		RFPAmbassador rfp_amb = new RFPAmbassador();
		RFPFederate rfp_federate = new RFPFederate(rfp_amb);

		rfp_federate.configure(configuration);
		rfp_federate.start();
		
		/*Scanner sc = new Scanner(System.in);
		String tmp = null;
		while(true){
			tmp = sc.nextLine();
			if(tmp.equals("q"))
				break;
			else if(tmp.equals("f")){
				rfp_federate.getMTR().setExecution_mode(MTRMode.MTR_GOTO_FREEZE);
				rfp_federate.updateInteraction(rfp_federate.getMTR());
				}
			else if(tmp.equals("r")){
				rfp_federate.getMTR().setExecution_mode(MTRMode.MTR_GOTO_RUN);
				rfp_federate.updateInteraction(rfp_federate.getMTR());
				}
			else if(tmp.equals("s")){
				rfp_federate.getMTR().setExecution_mode(MTRMode.MTR_GOTO_SHUTDOWN);
				rfp_federate.updateInteraction(rfp_federate.getMTR());
				}
		}*/
		
	}//main

}

