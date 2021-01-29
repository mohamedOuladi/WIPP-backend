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

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.NotFoundException;
import gov.nist.itl.ssd.wipp.backend.data.genericdata.genericfiles.GenericFileHandler;


/**
*
* @author Mohamed Ouladi <mohamed.ouladi@nist.gov>
*/
@Component
@RepositoryEventHandler
public class GenericDataEventHandler {

    private static final Logger LOGGER = Logger.getLogger(GenericDataEventHandler.class.getName());

    @Autowired
    private GenericDataRepository genericDataRepository;
    
    @Autowired
    private GenericFileHandler genericFileRepository;
    
    @Autowired
    private GenericDataLogic genericDataLogic;

    @Autowired
    CoreConfig config;

    @PreAuthorize("isAuthenticated()")
    @HandleBeforeCreate
    public void handleBeforeCreate(GenericData genericData) {
    	// Assert imagesCollection name is unique
    	genericDataLogic.assertCollectionNameUnique(
                genericData.getName());
        
        // Set creation date to current date
    	genericData.setCreationDate(new Date());

        // Set the owner to the connected user
    	genericData.setOwner(SecurityContextHolder.getContext().getAuthentication().getName());

        
        // Default import method is UPLOADED
//        if (genericData.getImportMethod() == null) {
//        	genericData.setImportMethod(ImagesCollectionImportMethod.UPLOADED);
//        }
//        
//        // Collections from Catalog are locked by default
//        if (genericData.getImportMethod() != null
//        	   && genericData.getImportMethod().equals(ImagesCollectionImportMethod.CATALOG)) {
//        	genericData.setLocked(true);
//        }
    }

    @HandleBeforeSave
    @PreAuthorize("isAuthenticated() and (hasRole('admin') or #imagesCollection.owner == principal.name)")
    public void handleBeforeSave(GenericData genericData) {
    	// Assert collection exists
        Optional<GenericData> result = genericDataRepository.findById(
        		genericData.getId());
    	if (!result.isPresent()) {
        	throw new NotFoundException("Generic Data collection with id " + genericData.getId() + " not found");
        }

    	GenericData oldTc = result.get();

    	// A public collection cannot become private
    	if (oldTc.isPubliclyShared() && !genericData.isPubliclyShared()){
            throw new ClientException("Can not set a public collection to private.");
        }
    	
    	// An unlocked collection cannot become public
    	if (!oldTc.isPubliclyShared() && genericData.isPubliclyShared() && !oldTc.isLocked()){
            throw new ClientException("Can not set an unlocked collection to public, please lock collection first.");
        }
    	
    	// Owner cannot be changed
        if (!Objects.equals(
        		genericData.getOwner(),
                oldTc.getOwner())) {
            throw new ClientException("Can not change owner.");
        }

    	// Creation date cannot be changed
        if (!Objects.equals(
        		genericData.getCreationDate(),
                oldTc.getCreationDate())) {
            throw new ClientException("Can not change creation date.");
        }

        // Import method cannot be changed
//        if (!Objects.equals(
//        		genericData.getImportMethod(),
//                oldTc.getImportMethod())) {
//            throw new ClientException("Can not change import method.");
//        }
        
        // Source job cannot be changed
        if (!Objects.equals(
        		genericData.getSourceJob(),
                oldTc.getSourceJob())) {
            throw new ClientException("Can not change source job.");
        }

        // Assert collection name is unique
        if (!Objects.equals(genericData.getName(), oldTc.getName())) {
        	genericDataLogic.assertCollectionNameUnique(
            		genericData.getName());
        }

        // Cannot unlock locked collection
        if (genericData.isLocked() != oldTc.isLocked()) {
            if (!genericData.isLocked()) {
                throw new ClientException("Can not unlock images collection.");
            }
            genericDataLogic.assertCollectionNotImporting(oldTc);
            genericDataLogic.assertCollectionHasNoImportError(oldTc);
        }
    }

    @HandleBeforeDelete
    @PreAuthorize("isAuthenticated() and (hasRole('admin') or #genericData.owner == principal.name)")
    public void handleBeforeDelete(GenericData genericData) {
    	// Assert collection exists
    	Optional<GenericData> result = genericDataRepository.findById(
    			genericData.getId());
    	if (!result.isPresent()) {
        	throw new NotFoundException("Generic data collection with id " + genericData.getId() + " not found");
        }

    	GenericData oldTc = result.get();
        
        // Locked collection cannot be deleted
    	genericDataLogic.assertCollectionNotLocked(oldTc);
    }

    @HandleAfterDelete
    public void handleAfterDelete(GenericData genericData) {
    	// Delete all geenricFiles from deleted collection
    	genericFileRepository.deleteAllInDb(genericData.getId(), false);
    	File genericDataFolder = new File (config.getGenericDatasFolder(), genericData.getId());
    	try {
    		FileUtils.deleteDirectory(genericDataFolder);
    	} catch (IOException e) {
    		LOGGER.log(Level.WARNING, "Was not able to delete the generic data collection folder " + genericDataFolder);
    	}	
    }

}
