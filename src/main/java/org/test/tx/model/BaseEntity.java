package org.test.tx.model;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

@MappedSuperclass
public class BaseEntity {
	@Version
	private Integer version;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	@Basic(optional = false)
	private Date created;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "LAST_UPDATED", nullable = false)
	@Basic(optional = false)
	private Date lastUpdated;
	@Column(name = "LAST_UPDATED_BY", nullable = true)
	@Basic(optional = true)
	private String lastUpdatedBy;
	
	public BaseEntity() {
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public String getLastUpdatedBy() {
		return lastUpdatedBy;
	}

	public void setLastUpdatedBy(String lastUpdatedBy) {
		this.lastUpdatedBy = lastUpdatedBy;
	}
}
