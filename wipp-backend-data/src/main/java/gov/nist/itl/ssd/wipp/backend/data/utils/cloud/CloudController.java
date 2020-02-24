package gov.nist.itl.ssd.wipp.backend.data.utils.cloud;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;

/**
*
* @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
*/
public abstract class CloudController {
	
	@Autowired
	private CoreConfig config;

    protected abstract void importDir(String cloudId, String fileId, String localDirPath);

	
}
