package gov.nist.itl.ssd.wipp.backend.data.utils.cloud.aws;

import java.io.File;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.MultipleFileDownload;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;

import gov.nist.itl.ssd.wipp.backend.data.utils.cloud.CloudController;

public class AmazonS3Controller extends CloudController{

	@Override
	protected void importDir(String bucketName, String keyName, String localDirPath) {	
		Regions clientRegion = Regions.US_EAST_1;
		importDirWithRegion(bucketName, keyName, localDirPath, clientRegion);
	}
	
	protected void importDirWithRegion(String bucketName, String keyName, String localDirPath, Regions region){
		downloadDir(bucketName, keyName, localDirPath, false, region);
	}
	
	private static void downloadDir(String bucket_name, String key_prefix,
			String dir_path, boolean pause, Regions region) {
		System.out.println("downloading to directory: " + dir_path +
				(pause ? " (pause)" : ""));
		
		AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
				.withRegion(region)
				.withCredentials(new ProfileCredentialsProvider())
				.build();
		
		System.out.println("downloading to directory: " + dir_path +
				(pause ? " (pause)" : ""));
		
		// snippet-start:[s3.java1.s3_xfer_mgr_download.directory]
		TransferManager xfer_mgr = TransferManagerBuilder.standard().withS3Client(s3Client).build();

		try {
			MultipleFileDownload xfer = xfer_mgr.downloadDirectory(
					bucket_name, key_prefix, new File(dir_path));
			// loop with Transfer.isDone()
			XferMgrProgress.showTransferProgress(xfer);
			// or block with Transfer.waitForCompletion()
			XferMgrProgress.waitForCompletion(xfer);
		} catch (AmazonServiceException e) {
			System.err.println(e.getErrorMessage());
			System.exit(1);
		}
		xfer_mgr.shutdownNow();
		// snippet-end:[s3.java1.s3_xfer_mgr_download.directory]
	}

}
