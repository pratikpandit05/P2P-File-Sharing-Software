
import ConfigurationParsers.PeerConfig;
import ConfigurationParsers.CommonFileParser;
import ConfigurationParsers.ParametersParser;
import ConfigurationParsers.FileFormatExxception;
import Communicators.CommunicationHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;

public class Peer

{

    private final String commonConfigFileName = System.getProperty("user.dir") + "/Common.cfg";
    private final String peerConfigFileName = System.getProperty("user.dir") + "/PeerInfo.cfg";
    private Map<String, CommunicationHandler> fileToTransceiverMap;
    private Map<String, Map<Integer, PeerConfig>> fileToPeerConfigMap;
    private Map<String, CommonFileParser> fileToCommonConfigMap = null;
    private final int myPeerID;
    private static final Logger logger = Logger.getLogger("A");
    
    public Peer(int myPeerID) throws FileFormatExxception, IOException, InterruptedException
    {
        this.myPeerID = myPeerID;
        this.fileToCommonConfigMap = new HashMap<String, CommonFileParser>();
        this.fileToPeerConfigMap = new HashMap<String, Map<Integer,PeerConfig>>();
        this.fileToTransceiverMap = new HashMap<String, CommunicationHandler>();
        this.startup();
    }

    private void startup() throws FileFormatExxception, IOException, InterruptedException
    {
        ParametersParser commonConfigReader = new ParametersParser(commonConfigFileName);
        CommonFileParser myCommonConfig = commonConfigReader.getCommonConfig();
        this.fileToCommonConfigMap.put(myCommonConfig.getFileName(), myCommonConfig);
        ParametersParser peerConfigReader = new ParametersParser(peerConfigFileName);
        Map<Integer, PeerConfig> peerConfigMap = peerConfigReader.getPeerConfigMap();
        this.fileToPeerConfigMap.put(myCommonConfig.getFileName(), peerConfigMap);
        
        CommunicationHandler aTransceiver = new CommunicationHandler(myCommonConfig, peerConfigMap, myPeerID);
        this.fileToTransceiverMap.put(myCommonConfig.getFileName(), aTransceiver);
        aTransceiver.start();
        logger.info("Transceiver started for PeerID = " + this.myPeerID);
    }
    
    public static void main(String[] args) throws NumberFormatException, FileFormatExxception, IOException, InterruptedException
    {
        if(args.length < 1)
        {
            System.out.println("Usage: java Peer <PeerID>");
        }
        
        Peer p = new Peer(Integer.parseInt(args[0]));
           
    }
}