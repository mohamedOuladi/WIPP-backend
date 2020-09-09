/*
 * This software was developed at the National Institute of Standards and
 * Technology by employees of the Federal Government in the course of
 * their official duties. Pursuant to title 17 Section 105 of the United
 * States Code this software is not subject to copyright protection and is
 * in the public domain. This software is an experimental system. NIST assumes
 * no responsibility whatsoever for its use by other parties, and makes no
 * guarantees, expressed or implied, about its quality, reliability, or
 * any other characteristic. We would appreciate acknowledgement if the
 * software is used.
 */
package gov.nist.itl.ssd.wipp.backend.data.genericdata;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.model.data.BaseDataHandler;
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataHandler;
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.model.job.JobExecutionException;
import gov.nist.itl.ssd.wipp.backend.data.genericdata.genericfiles.GenericFileHandler;

/**
*
* @author Mohamed Ouladi <mohamed.ouladi@nist.gov>
*/
@Component("genericDataDataHandler")
public class GenericDataDataHandler extends BaseDataHandler implements DataHandler{

	@Autowired
	CoreConfig config;

	@Autowired
	private GenericDataRepository genericDataRepository;
	

    @Autowired
    private GenericFileHandler genericFileHandler;

	@Override
	public void importData(Job job, String outputName) throws JobExecutionException {
		GenericData genericData = new GenericData(job, outputName);
		genericDataRepository.save(genericData);
		
        try {
            File jobOutputTempFolder = getJobOutputTempFolder(job.getId(), outputName);
            genericFileHandler.importFolder(genericData.getId(), jobOutputTempFolder);
            setOutputId(job, outputName, genericData.getId());
        } catch (IOException ex) {
        	genericDataRepository.delete(genericData);
            throw new JobExecutionException("Cannot move Generic Data to final destination.");
        }
        setOutputId(job, outputName, genericData.getId());
		
		// search for metadata file
		File genericDataFolder = new File(config.getGenericDatasFolder(), genericData.getId());
		File[] metadataFiles = genericDataFolder.listFiles(new FilenameFilter() {
		    public boolean accept(File dir, String name) {
		        return name.equals("data-info.json");
		    }
		});
		
		// parse metadata file
		if (metadataFiles != null && metadataFiles.length != 0){
			File metadataFile = metadataFiles[0];
			JSONParser parser = new JSONParser();
			try {
				Object obj = parser.parse(new FileReader(metadataFile.getAbsolutePath()));
				
		        // typecasting obj to JSONObject 
		        JSONObject jo = (JSONObject) obj; 
		          
		        // get type, description and metadata 
		        String type = (String) jo.get("type"); 
		        String description = (String) jo.get("description"); 
		        String metadata = (String) jo.get("metadata");
				
				genericData.setType(type);
				genericData.setDescription(description);
				genericData.setMetadata(metadata);
				
				// updating generic data
				genericDataRepository.save(genericData);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
    public String exportDataAsParam(String value) {
        String genericDataId = value;
        String genericDataPath;

        // check if the input of the job is the output of another job and if so return the associated path
        String regex = "\\{\\{ (.*)\\.(.*) \\}\\}";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(genericDataId);
        if (m.find()) {
            String jobId = m.group(1);
            String outputName = m.group(2);
            genericDataPath = getJobOutputTempFolder(jobId, outputName).getAbsolutePath();
        }
        // else return the path of the tensorflow model
        else {
            File genericDataFolder = new File(config.getGenericDatasFolder(), genericDataId);
            genericDataPath = genericDataFolder.getAbsolutePath();

        }
        genericDataPath = genericDataPath.replaceFirst(config.getStorageRootFolder(),config.getContainerInputsMountPath());
        return genericDataPath;

    }
}
