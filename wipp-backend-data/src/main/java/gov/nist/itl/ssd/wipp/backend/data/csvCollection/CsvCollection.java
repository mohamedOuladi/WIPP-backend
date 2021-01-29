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
package gov.nist.itl.ssd.wipp.backend.data.csvCollection;

import com.fasterxml.jackson.annotation.JsonProperty;
import gov.nist.itl.ssd.wipp.backend.core.model.job.Job;
import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.IdExposed;
import gov.nist.itl.ssd.wipp.backend.core.rest.annotation.ManualRef;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 *
 * @author Mohamed Ouladi <mohamed.ouladi@nist.gov>
 */
@IdExposed
@Document
public class CsvCollection {

	@Id
	private String id;

	private String name;

	private String owner;

	private boolean locked;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private int numberOfImportErrors;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private int numberImportingCsv;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private int numberOfCsvFiles;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private long csvTotalSize;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	private Date creationDate;

	@Indexed(unique = true, sparse = true)
	@ManualRef(Job.class)
	private String sourceJob;

	private boolean publiclyShared;


	public CsvCollection() {
	}

	public CsvCollection(String name, boolean locked){
		this.name = name;
		this.creationDate = new Date();
		this.locked = locked;
	}

	public CsvCollection(Job job){
		this.name = job.getName();
		this.sourceJob = job.getId();
		this.creationDate = new Date();
		this.locked = true;
	}

	public CsvCollection(Job job, String outputName) {
		this.name = job.getName() + "-" + outputName;
		this.sourceJob = job.getId();
		this.creationDate = new Date();
		this.locked = true;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public String getSourceJob() {
		return sourceJob;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public boolean isPubliclyShared() {
		return publiclyShared;
	}

	public void setPubliclyShared(boolean publiclyShared) {
		this.publiclyShared = publiclyShared;
	}


	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {this.locked = locked;}

	public int getNumberOfImportErrors() {
		return numberOfImportErrors;
	}

	public int getNumberImportingCsv() {
		return numberImportingCsv;
	}

	public int getNumberOfCsvFiles() {
		return numberOfCsvFiles;
	}

}
