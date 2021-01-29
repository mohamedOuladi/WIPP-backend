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
package gov.nist.itl.ssd.wipp.backend.data.pyramid;

import java.io.File;
import java.util.Optional;

import gov.nist.itl.ssd.wipp.backend.core.model.data.BaseDataHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.model.data.DataHandler;
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.model.job.JobExecutionException;

/**
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
@Component("pyramidDataHandler")
public class PyramidDataHandler extends BaseDataHandler implements DataHandler{


    @Autowired
    CoreConfig config;

    @Autowired
    private PyramidRepository pyramidRepository;

    public PyramidDataHandler() {
    }

    @Override
    public void importData(Job job, String outputName) throws JobExecutionException {
        Pyramid outputPyramid = new Pyramid(job, outputName);
        // Set pyramid owner to job owner
        outputPyramid.setOwner(job.getOwner());
        // Set pyramid to private
        outputPyramid.setPubliclyShared(false);
        pyramidRepository.save(outputPyramid);

        File pyramidFolder = new File(config.getPyramidsFolder(), outputPyramid.getId());
        pyramidFolder.mkdirs();

        File tempOutputDir = getJobOutputTempFolder(job.getId(), outputName);
        boolean success = tempOutputDir.renameTo(pyramidFolder);
        if (!success) {
            pyramidRepository.delete(outputPyramid);
            throw new JobExecutionException("Cannot move pyramid to final destination.");
        }

        setOutputId(job, outputName, outputPyramid.getId());
    }

    @Override
    public String exportDataAsParam(String value) {
        String pyramidId = value;
        File inputPyramidFolder = new File(config.getPyramidsFolder(), pyramidId);
        String pyramidPath = inputPyramidFolder.getAbsolutePath();
        return pyramidPath;
    }

    @Override
    public void setDataToPublic(String value) {
    	Optional<Pyramid> optPyramid = pyramidRepository.findById(value);
        if(optPyramid.isPresent()) {
        	Pyramid pyramid = optPyramid.get();
            if (!pyramid.isPubliclyShared()) {
            	pyramid.setPubliclyShared(true);
            	pyramidRepository.save(pyramid);
            }
        }
    }
}
