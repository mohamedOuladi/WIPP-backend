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
package gov.nist.itl.ssd.wipp.backend.data.stitching;

import gov.nist.itl.ssd.wipp.backend.data.stitching.timeslices.StitchingVectorTimeSlice;
import java.io.File;
import java.util.List;

/**
*
* @author Antoine Vandecreme
*/
public interface StitchingVectorRepositoryCustom {

    // not exported
	void setTimeSlices(String stitchingVectorId,
            List<StitchingVectorTimeSlice> timeSlices);

    // not exported
    List<StitchingVectorTimeSlice> getTimeSlices(String stitchingVectorId);

    // not exported
    File getStatisticsFile(String stitchingVectorId);

}