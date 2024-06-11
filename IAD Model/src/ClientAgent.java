import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ClientAgent extends Agent {
    int x;
    int y;

    protected void setup() {
        // Initialisation de l'agent Point
        Object[] args = getArguments();
        if (args != null && args.length == 2) {
            x = Integer.parseInt(args[0].toString());
            y = Integer.parseInt(args[1].toString());
        } else {
            System.err.println("L'agent Point nécessite des coordonnées x et y en tant qu'arguments.");
            doDelete();
        }

        registerService("Point*");
        System.out.println(getLocalName() + ": Initial position : x: " + x + ", y: " + y);
        addBehaviour(new CoordinateRequestBehaviour());
    }

    private void registerService(String serviceType) {
        // Enregistre le service auprès du Directory Facilitator (DF)
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType(serviceType);
            sd.setName("PointService");
            dfd.addServices(sd);

            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private class CoordinateRequestBehaviour extends CyclicBehaviour {
        public void action() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // Écoute les messages entrants
            ACLMessage msg = receive();

            if (msg != null) {
                // Gère la demande de coordonnées entrante
                if (msg.getPerformative() == ACLMessage.REQUEST && msg.getContent().equals("Position?")) {
                    // Répond avec les coordonnées
                    System.out.println(getLocalName() + ": Received request for coordinates from " + msg.getSender().getLocalName());
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent(x + "," + y);
                    send(reply);
                    System.out.println(getLocalName() + ": Sent coordinates to " + msg.getSender().getLocalName());

                    MessageTemplate proposeTemplate = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
                    ACLMessage propose = myAgent.blockingReceive(proposeTemplate);

                    if (propose != null) {
                        // Traite la proposition du VehicleAgent
                        System.out.println(getLocalName() + ": Received proposal from " + propose.getSender().getLocalName());

                        // Évalue la proposition (simple ici, mais vous pouvez ajouter une logique plus complexe)

                        // Accepte la proposition
                        ACLMessage accept = propose.createReply();
                        accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        send(accept);
                        System.out.println(getLocalName() + ": Accepted proposal from " + propose.getSender().getLocalName());
                    } else {
                        System.out.println(getLocalName() + ": No proposal received.");
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    // Gère d'autres types de messages si nécessaire
                    //System.out.println(getLocalName() + ": Received unexpected message: " + msg.getContent());
                }
            } else {
                // Bloque le comportement jusqu'à ce qu'un message soit reçu
                block();
            }
        }
    }
}
