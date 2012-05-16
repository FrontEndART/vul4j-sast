package org.ovirt.engine.ui.userportal.uicommon;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.EventDefinition;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.common.uicommon.ClientAgentType;
import org.ovirt.engine.ui.common.uicommon.model.UiCommonInitEvent;
import org.ovirt.engine.ui.common.uicommon.model.UiCommonInitEvent.UiCommonInitHandler;
import org.ovirt.engine.ui.uicommonweb.Configurator;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.MainTabBasicPresenter;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class UserPortalConfigurator extends Configurator implements IEventListener, UiCommonInitHandler {

    public static final String DOCUMENTATION_GUIDE_PATH = "User_Portal_Guide/index.html"; //$NON-NLS-1$

    public EventDefinition spiceVersionFileFetchedEvent_Definition =
            new EventDefinition("spiceVersionFileFetched", UserPortalConfigurator.class); //$NON-NLS-1$
    public Event spiceVersionFileFetchedEvent = new Event(spiceVersionFileFetchedEvent_Definition);

    public EventDefinition usbFilterFileFetchedEvent_Definition =
            new EventDefinition("usbFilterFileFetched", UserPortalConfigurator.class); //$NON-NLS-1$
    public Event usbFilterFileFetchedEvent = new Event(usbFilterFileFetchedEvent_Definition);

    private final Provider<MainTabBasicPresenter> basicPresenter;

    private static final ClientAgentType clientAgentType = new ClientAgentType();

    @Inject
    public UserPortalConfigurator(Provider<MainTabBasicPresenter> basicPresenter, EventBus eventBus) {
        super();
        this.basicPresenter = basicPresenter;
        eventBus.addHandler(UiCommonInitEvent.getType(), this);

        // Add event listeners
        spiceVersionFileFetchedEvent.addListener(this);
        usbFilterFileFetchedEvent.addListener(this);

        // Update USB filters
        updateUsbFilter();
    }

    public void updateUsbFilter() {
        fetchFile(getSpiceBaseURL() + "consoles/spice/usbfilter.txt", usbFilterFileFetchedEvent); //$NON-NLS-1$
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args) {
        if (ev.equals(spiceVersionFileFetchedEvent_Definition)) {
            Version spiceVersion = parseVersion(((FileFetchEventArgs) args).getFileContent());
            setSpiceVersion(spiceVersion);
        } else if (ev.equals(usbFilterFileFetchedEvent_Definition)) {
            String usbFilter = ((FileFetchEventArgs) args).getFileContent();
            setUsbFilter(usbFilter);
        }
    }

    /**
     * Returns true if the basic view is shown, else returns false
     */
    @Override
    public boolean getSpiceFullScreen() {
        return basicPresenter.get().isVisible();
    }

    @Override
    protected Event getSpiceVersionFileFetchedEvent() {
        return spiceVersionFileFetchedEvent;
    }

    @Override
    public void onUiCommonInit(UiCommonInitEvent event) {
        updateDocumentationBaseURL();
    }

    @Override
    protected String removeModulName(String moduleName) {
        return moduleName.replace(USERPORTAL_ROOT_FOLDER, "") + "/"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected String clientBrowserType() {
        return clientAgentType.browser;
    }

    @Override
    protected String clientOsType() {
        return clientAgentType.os;
    }

    @Override
    protected String clientPlatformType() {
        return clientAgentType.getPlatform();
    }

}
