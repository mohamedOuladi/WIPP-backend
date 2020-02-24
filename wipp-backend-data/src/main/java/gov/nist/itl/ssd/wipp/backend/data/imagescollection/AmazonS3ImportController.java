package gov.nist.itl.ssd.wipp.backend.data.imagescollection;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.regions.Regions;

import gov.nist.itl.ssd.wipp.backend.core.CoreConfig;
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.rest.exception.ClientException;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollection.ImagesCollectionImportMethod;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.ImagesCollectionCopyController.CopyRequestBody;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.files.FileHandler;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.images.Image;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.images.ImageConversionService;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.images.ImageHandler;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.images.ImageRepository;
import gov.nist.itl.ssd.wipp.backend.data.imagescollection.metadatafiles.MetadataFileHandler;
import gov.nist.itl.ssd.wipp.backend.data.utils.cloud.aws.AmazonS3Controller;

/**
 *
 * @author Mohamed Ouladi <mohamed.ouladi at nist.gov>
 */

@RestController
@RequestMapping(CoreConfig.BASE_URI + "/imagesCollections/{imagesCollectionId}/amazons3")
public class AmazonS3ImportController extends AmazonS3Controller{

	@Autowired
	CoreConfig config;

	@Autowired
	private ImagesCollectionRepository imagesCollectionRepository;

	@Autowired
	private MetadataFileHandler metadataRepository;

	@Autowired
	private ImageHandler imageHandler;

	@Autowired
	private ImageRepository imageRepository;

	@Autowired
	private MetadataFileHandler metadataHandler;

	@Autowired
	private ImageConversionService imageConversionService;


	@RequestMapping(
			value = "",
			method = RequestMethod.POST)
	public void importFromAamzonS3(
			@PathVariable("imagesCollectionId") String imagesCollectionId,
			@RequestParam("bucketName") String bucketName,
			@RequestParam("clientRegion") String clientRegion,
			@RequestParam("keyName") String keyName) {

		System.out.println("Hello");
		System.out.println(clientRegion);

		Optional<ImagesCollection> tc = imagesCollectionRepository.findById(imagesCollectionId);

		if (!tc.isPresent()) {
			throw new ResourceNotFoundException(
					"Images collection " + imagesCollectionId + " not found.");
		}

		ImagesCollection imagesCollection = tc.get();

		// Check if the images collection is not empty
		if(imagesCollection.getNumberOfImages() != 0  || imagesCollection.getNumberOfMetadataFiles() != 0) {
			throw new ClientException("Collection is not empty.");
		}

		Regions region = Regions.fromName(clientRegion);

		try {
			File imagesCollectionTempFolder = new File(config.getCollectionsUploadTmpFolder(), imagesCollectionId);
			String localDirPath = imagesCollectionTempFolder.toString();
			importDirWithRegion(bucketName, keyName, localDirPath, region);

			// Import and convert images
			imageHandler.addAllInDbFromTemp(imagesCollectionId);
			List<Image> images = imageRepository.findByImagesCollection(imagesCollectionId);

			for(Image image : images) {
				imageConversionService.submitImageToExtractor(image);
			}

			// Import metadata files
			File metadataFolder = new File(imagesCollectionTempFolder, "metadata_files");
			if(metadataFolder.exists()) {
				importFolder(metadataHandler, metadataFolder, imagesCollectionId);
			}

		} catch (IOException ex) {
			throw new ClientException("Error while importing data.");
		}	
	}

	private void importFolder(FileHandler fileHandler, File file, String id) throws IOException {
		fileHandler.importFolder(id, file);
	}

}
