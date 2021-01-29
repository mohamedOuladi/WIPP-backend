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
package gov.nist.itl.ssd.wipp.backend.data.tensorflowmodels;

import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Mylene Simon <mylene.simon at nist.gov>
 */
@Component
public class TensorflowModelLogic {

    @Autowired
    private TensorflowModelRepository tensorflowModelRepository;

    public void assertTensorflowModelNameUnique(String name) {
        if (tensorflowModelRepository.countByName(name) != 0) {
            throw new ClientException("A tensorflow model named \""
                    + name + "\" already exists.");
        }
    }

}
