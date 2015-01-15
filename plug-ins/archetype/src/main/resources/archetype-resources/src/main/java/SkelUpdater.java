/*
 * Add a nice copyright message here. Since plug-ins are not linked at
 * compile time to the source, any OSS license (even GPL) will do.
 */

package ${project.groupId}.${project.artifactId};

import java.util.List;

import org.osgi.framework.BundleContext;

import eu.sqooss.service.db.StoredProject;
import eu.sqooss.service.logging.Logger;
import eu.sqooss.service.updater.MetadataUpdater;
import eu.sqooss.service.updater.Updater;
import eu.sqooss.service.updater.UpdaterService.UpdaterStage;

/**
 * A metadata updater converts raw data to Alitheia Core database metadata.
 */
@Updater(descr = "Skeleton updater", 
        protocols = {"skel"},
        stage = UpdaterStage.IMPORT, 
        mnem = "SKEL")
public class SkelUpdater implements MetadataUpdater {
    
    private StoredProject sp;
    private Logger log;
    
    public SkelUpdater() {}
   
    public void setUpdateParams(StoredProject sp, Logger l) {
        this.sp = sp;
        this.log = l;
    }

    /**
     * Updates the metadata. Should support incremental update based on what
     * is currently in the database and what is the state of the raw data.
     * Of couse the updater implementor knows better.
     * 
     * Significant is never to mask, catch or otherwise manipulate exceptions
     * thrown while the updater is running. Let them be thrown. The job
     * mechanism will catch and log them approprietaly. 
     * 
     */
    public void update() throws Exception {
        
    }
    
    /**
     * This method should return a sensible representation of progress. 
     */
    @Override
    public int progress() {
        return 0;
    }
}

// vi: ai nosi sw=4 ts=4 expandtab
