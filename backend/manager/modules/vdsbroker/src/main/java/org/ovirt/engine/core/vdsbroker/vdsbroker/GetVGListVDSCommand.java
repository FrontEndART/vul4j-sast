package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;

@SuppressWarnings("unchecked")
public class GetVGListVDSCommand<P extends VdsIdVDSCommandParametersBase> extends VdsBrokerCommand<P> {
    protected VGListReturnForXmlRpc _result;

    public GetVGListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        _result = getBroker().getVGList();
        proceedProxyReturnValue();
        setReturnValue(parseVGList(_result.vgList));
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _result.mStatus;
    }

    protected java.util.ArrayList<StorageDomain> parseVGList(Map<String, Object>[] vgList) {
        java.util.ArrayList<StorageDomain> result = new java.util.ArrayList<StorageDomain>(vgList.length);
        for (Map<String, Object> vg : vgList) {
            StorageDomain sDomain = new StorageDomain();
            if (vg.containsKey("name")) {
                try {
                    sDomain.setId(new Guid(vg.get("name").toString()));
                } catch (java.lang.Exception e) {
                    sDomain.setStorageName(vg.get("name").toString());
                }
            }
            sDomain.setStorage(vg.get("vgUUID").toString());
            Long size = IrsBrokerCommand.AssignLongValue(vg, "vgfree");
            if (size != null) {
                sDomain.setAvailableDiskSize((int) (size / IrsBrokerCommand.BYTES_TO_GB));
            }
            size = IrsBrokerCommand.AssignLongValue(vg, "vgsize");
            if (size != null && sDomain.getAvailableDiskSize() != null) {
                sDomain.setUsedDiskSize((int) (size / IrsBrokerCommand.BYTES_TO_GB)
                        - sDomain.getAvailableDiskSize());
            }
            if (vg.containsKey("vgtype")) {
                sDomain.setStorageType(EnumUtils.valueOf(StorageType.class, vg.get("vgtype").toString(), true));
            } else {
                sDomain.setStorageType(StorageType.ALL);
            }
            result.add(sDomain);
        }
        return result;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }
}
