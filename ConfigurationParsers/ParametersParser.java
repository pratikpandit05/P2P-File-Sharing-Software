/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ConfigurationParsers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ParametersParser
{
    private BufferedReader fileReader;

    public ParametersParser(String filePath) throws FileNotFoundException
    {
        this.fileReader = null;
        this.tryOpeningFile(filePath);
    }

    private void tryOpeningFile(String filePath) throws FileNotFoundException
    {
        fileReader = new BufferedReader(new InputStreamReader(
                new FileInputStream(filePath)));
    }

    public Map<Integer, PeerConfig> getPeerConfigMap() throws FileFormatExxception,
            IOException
    {
        if (this.fileReader == null)
        {
            throw new FileFormatExxception("Couldn't open the file");
        }
        Map<Integer, PeerConfig> peerConfigMap = new HashMap<Integer, PeerConfig>();
        while (true)
        {
            String aLine = fileReader.readLine();

            if (aLine == null)
                break;

            String[] tokens = aLine.split(" ");
            try
            {
                fillTokens(tokens, peerConfigMap);
            } catch (Exception ex)
            {
                throw new FileFormatExxception(ex.getMessage());
            }
        }

        return peerConfigMap;
    }

    private void fillTokens(String[] tokens, Map<Integer, PeerConfig> peerConfigMap)
    {
        PeerConfig peerConfig = new PeerConfig();
        peerConfig.setHostName(tokens[1]);
        peerConfig.setListeningPort(Integer.parseInt(tokens[2]));
        peerConfig.setHasFile(Integer.parseInt(tokens[3]) == 1 ? true : false);

        // add this peer to list
        peerConfigMap.put(Integer.parseInt(tokens[0]),peerConfig);
    }

    public CommonFileParser getCommonConfig() throws FileFormatExxception,
            IOException
    {
        if (this.fileReader == null)
        {
            throw new FileFormatExxception("Couldn't open the file");
        }
        
        CommonFileParser commonConfig = new CommonFileParser();
        
        while (true)
        {
            String aLine = fileReader.readLine();

            if (aLine == null)
                break;

            String[] tokens = aLine.split(" ");
            try
            {
                fillTokens(tokens, commonConfig);
            } catch (Exception ex)
            {
                throw new FileFormatExxception(ex.getMessage());
            }
        }

        return commonConfig;
    }

    private void fillTokens(String[] tokens, CommonFileParser commonConfig)
    {
        if (tokens[0].equalsIgnoreCase("NumberOfPreferredNeighbors"))
        {
            commonConfig.setNumPreferredNeighbours(Integer.parseInt(tokens[1]));
        } else if (tokens[0].equalsIgnoreCase("UnchokingInterval"))
        {
            commonConfig.setUnchokingInterval(Integer.parseInt(tokens[1]));
        } else if (tokens[0].equalsIgnoreCase("OptimisticUnchokingInterval"))
        {
            commonConfig.setOptimisticUnchokingInterval(Integer
                    .parseInt(tokens[1]));
        } else if (tokens[0].equalsIgnoreCase("FileName"))
        {
            commonConfig.setFileName(tokens[1]);
        } else if (tokens[0].equalsIgnoreCase("FileSize"))
        {
            commonConfig.setFSize(Integer.parseInt(tokens[1]));
        } else if (tokens[0].equalsIgnoreCase("PieceSize"))
        {
            commonConfig.setPieceSize(Integer.parseInt(tokens[1]));
        } 
    }
}
