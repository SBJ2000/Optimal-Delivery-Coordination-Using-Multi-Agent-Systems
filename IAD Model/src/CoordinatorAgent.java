import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CoordinatorAgent extends Agent {

    protected void setup() {

        // Add behavior to receive visited points information
        // Register the Coordinator agent with the "Coordinator" service type
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("Coordinator");
        sd.setName("CoordinatorAgent");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
            System.out.println("Coordinator Agent " + getLocalName() + " is ready.");

            // Add behavior to receive visited points information
            addBehaviour(new ReceiveVisitedPointsBehaviour());
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        addBehaviour(new ReceiveVisitedPointsBehaviour());
    }

    private class ReceiveVisitedPointsBehaviour extends CyclicBehaviour {
        public void action() {
            // Définir un modèle de message pour correspondre aux messages de type INFORM
            MessageTemplate informTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);

            // Recevoir un message correspondant au modèle
            ACLMessage inform = receive(informTemplate);

            if (inform != null) {
                // Récupérer le contenu du message
                String content = inform.getContent();

                // Traiter le message en fonction de son contenu
                if (content.startsWith("VisitedPoints:")) {
                    // Si le message contient des informations sur les points visités
                    String visitedPointsInfo = content.substring("VisitedPoints:".length());
                    System.out.println(getLocalName() + ": Client delivered so far " + visitedPointsInfo);
                } else if (inform.getContent().startsWith("finish")) {
                    // Si le message indique la fin de la livraison
                    System.out.println(getLocalName() + ": Great Job Delivery Agent! Have a Good Day!!");
                } else {
                    // Bloquer le comportement si le message ne correspond à aucun des cas ci-dessus
                    block();
                }
            }
        }
    }

}