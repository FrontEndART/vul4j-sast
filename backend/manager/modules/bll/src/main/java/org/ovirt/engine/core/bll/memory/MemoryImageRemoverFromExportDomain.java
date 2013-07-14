package org.ovirt.engine.core.bll.memory;

import java.util.List;

import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.vdscommands.DeleteImageGroupVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;

public class MemoryImageRemoverFromExportDomain extends MemoryImageRemover {

    private Guid storagePoolId;
    private Guid storageDomainId;
    protected Boolean cachedPostZero;
    private VM vm;

    public MemoryImageRemoverFromExportDomain(VM vm, TaskHandlerCommand<?> enclosingCommand,
            Guid storagePoolId, Guid storageDomainId) {
        super(enclosingCommand);
        this.vm = vm;
        this.storagePoolId = storagePoolId;
        this.storageDomainId = storageDomainId;
    }

    @Override
    protected DeleteImageGroupVDSCommandParameters buildDeleteMemoryImageParams(List<Guid> guids) {
        return new DeleteImageGroupVDSCommandParameters(
                guids.get(1), guids.get(0), guids.get(2), isPostZero(), false);
    }

    @Override
    protected DeleteImageGroupVDSCommandParameters buildDeleteMemoryConfParams(List<Guid> guids) {
        return new DeleteImageGroupVDSCommandParameters(
                guids.get(1), guids.get(0), guids.get(4), isPostZero(), false);
    }

    /**
     * We set the post zero field on memory image deletion from export domain as we do
     * when it is deleted from data domain even though the export domain is NFS and NFS
     * storage do the wipe on its own, in order to be compliance with the rest of the
     * code that do the same, and to be prepared for supporting export domains which
     * are not NFS.
     */
    protected boolean isPostZero() {
        if (cachedPostZero == null) {
            // check if one of the disks is marked with wipe_after_delete
            cachedPostZero =
                    vm.getDiskMap().values().contains(new Object() {
                        @Override
                        public boolean equals(Object obj) {
                            return ((Disk) obj).isWipeAfterDelete();
                        }
                    });
        }
        return cachedPostZero;
    }

    @Override
    protected boolean isMemoryStateRemovable(String memoryVolume) {
        return !memoryVolume.isEmpty();
    }

    @Override
    public void removeMemoryVolume(String memoryVolumes) {
        super.removeMemoryVolume(
                MemoryUtils.changeStorageDomainAndPoolInMemoryState(
                        memoryVolumes, storageDomainId, storagePoolId));
    }
}
