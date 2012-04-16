package org.ovirt.engine.ui.webadmin.widget.footer;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTabModelProvider;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.common.widget.table.column.AuditLogSeverityColumn;
import org.ovirt.engine.ui.common.widget.table.column.FullDateTimeColumn;
import org.ovirt.engine.ui.common.widget.table.column.ImageResourceColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.AlertFirstRowModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.AlertModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.AlertModelProvider.AlertCountChangeHandler;
import org.ovirt.engine.ui.webadmin.uicommon.model.EventFirstRowModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.EventModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.TaskFirstRowModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.TaskModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.TaskModelProvider.TaskCountChangeHandler;
import org.ovirt.engine.ui.webadmin.widget.table.column.TaskStatusColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

public class AlertsEventsFooterView extends Composite implements AlertCountChangeHandler, TaskCountChangeHandler {

    interface WidgetUiBinder extends UiBinder<Widget, AlertsEventsFooterView> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    Style style;

    @UiField
    SimplePanel tablePanel;

    @UiField
    SimplePanel widgetPanel;

    @UiField
    SimplePanel firstRowTablePanel;

    @UiField
    ToggleButton alertButton;

    @UiField
    ToggleButton eventButton;

    @UiField
    ToggleButton taskButton;

    @UiField
    PushButton expandButton;

    @UiField
    PushButton collapseButton;

    @UiField
    Label message;

    SimpleActionTable<AuditLog> alertsTable;
    SimpleActionTable<AuditLog> eventsTable;
    TasksTree tasksTree;
    SimpleActionTable<AuditLog> _alertsTable;
    SimpleActionTable<AuditLog> _eventsTable;
    SimpleActionTable<Job> _tasksTable;

    String buttonUpStart;
    String buttonUpStretch;
    String buttonUpEnd;
    String buttonDownStart;
    String buttonDownStretch;
    String buttonDownEnd;

    private final ApplicationTemplates templates;
    private final ApplicationResources resources;
    private final SafeHtml alertImage;

    public AlertsEventsFooterView(AlertModelProvider alertModelProvider,
            AlertFirstRowModelProvider alertFirstRowModelProvider,
            EventModelProvider eventModelProvider,
            EventFirstRowModelProvider eventFirstRowModelProvider,
            TaskModelProvider taskModelProvider,
            TaskFirstRowModelProvider taskFirstRowModelProvider,
            ApplicationResources resources,
            ApplicationTemplates templates,
            EventBus eventBus,
            ClientStorage clientStorage,
            CommonApplicationConstants commonConstants) {
        this.resources = resources;
        this.templates = templates;
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        initButtonHandlers();
        alertModelProvider.setAlertCountChangeHandler(this);
        taskModelProvider.setTaskCountChangeHandler(this);

        alertsTable = createActionTable(alertModelProvider);
        alertsTable.setBarStyle(style.barStyle());
        initTable(alertsTable);

        _alertsTable = createActionTable(alertFirstRowModelProvider);
        _alertsTable.setBarStyle(style.barStyle());
        _alertsTable.getElement().getStyle().setOverflowY(Overflow.HIDDEN);
        initTable(_alertsTable);

        eventsTable = createActionTable(eventModelProvider);
        eventsTable.setBarStyle(style.barStyle());
        initTable(eventsTable);

        _eventsTable = createActionTable(eventFirstRowModelProvider);
        _eventsTable.setBarStyle(style.barStyle());
        _eventsTable.getElement().getStyle().setOverflowY(Overflow.HIDDEN);
        initTable(_eventsTable);

        tasksTree = new TasksTree(resources, commonConstants);
        tasksTree.updateTree(taskModelProvider.getModel());

        _tasksTable =
                new SimpleActionTable<Job>(taskFirstRowModelProvider, getTableResources(), eventBus, clientStorage);
        _tasksTable.setBarStyle(style.barStyle());
        _tasksTable.getElement().getStyle().setOverflowY(Overflow.HIDDEN);
        initTaskTable(_tasksTable);

        taskButton.setValue(false);
        alertButton.setValue(false);
        eventButton.setValue(true);
        message.setText("Last Message:");
        collapseButton.setVisible(false);

        tablePanel.clear();
        firstRowTablePanel.clear();
        tablePanel.add(eventsTable);
        firstRowTablePanel.add(_eventsTable);

        String image = AbstractImagePrototype.create(resources.alertConfigureImage()).getHTML();
        alertImage = SafeHtmlUtils.fromTrustedString(image);

        // no body is invoking the alert search (timer)
        alertModelProvider.getModel().Search();

        // no body is invoking the alert search (timer)
        taskModelProvider.getModel().Search();

        updateButtonResources();
        updateEventsButton();
        updateTaskButton(0);
        setAlertCount(0);
    }

    SimpleActionTable<AuditLog> createActionTable(SearchableTabModelProvider<AuditLog, ?> modelProvider) {
        return new SimpleActionTable<AuditLog>(modelProvider, getTableResources(),
                ClientGinjectorProvider.instance().getEventBus(),
                ClientGinjectorProvider.instance().getClientStorage());
    }

    AlertsEventsFooterResources getTableResources() {
        return GWT.<AlertsEventsFooterResources> create(AlertsEventsFooterResources.class);
    }

    @Override
    public void onAlertCountChange(int count) {
        setAlertCount(count);
    }

    @Override
    public void onRunningTasksCountChange(int count) {
        updateTaskButton(count);
    }

    void setAlertCount(int count) {

        String countStr = count + " " + "Alerts";

        SafeHtml up = templates.alertEventButton(alertImage, countStr,
                buttonUpStart, buttonUpStretch, buttonUpEnd, style.alertButtonUpStyle());
        SafeHtml down = templates.alertEventButton(alertImage, countStr,
                buttonDownStart, buttonDownStretch, buttonDownEnd, style.alertButtonDownStyle());

        alertButton.getUpFace().setHTML(up);
        alertButton.getDownFace().setHTML(down);
    }

    private void updateTaskButton(int count) {
        String tasksGrayImageSrc = AbstractImagePrototype.create(resources.iconTask()).getHTML();
        SafeHtml tasksGrayImage = SafeHtmlUtils.fromTrustedString(tasksGrayImageSrc);

        SafeHtml up = templates.alertEventButton(tasksGrayImage, "Tasks (" + count + ")",
                buttonUpStart, buttonUpStretch, buttonUpEnd, style.taskButtonUpStyle());
        SafeHtml down = templates.alertEventButton(tasksGrayImage, "Tasks (" + count + ")",
                buttonDownStart, buttonDownStretch, buttonDownEnd, style.taskButtonDownStyle());

        taskButton.getUpFace().setHTML(up);
        taskButton.getDownFace().setHTML(down);
    }

    void updateEventsButton() {
        String eventsGrayImageSrc = AbstractImagePrototype.create(resources.eventsGrayImage()).getHTML();
        SafeHtml eventsGrayImage = SafeHtmlUtils.fromTrustedString(eventsGrayImageSrc);

        SafeHtml up = templates.alertEventButton(eventsGrayImage, "Events",
                buttonUpStart, buttonUpStretch, buttonUpEnd, style.eventButtonUpStyle());
        SafeHtml down = templates.alertEventButton(eventsGrayImage, "Events",
                buttonDownStart, buttonDownStretch, buttonDownEnd, style.eventButtonDownStyle());

        eventButton.getUpFace().setHTML(up);
        eventButton.getDownFace().setHTML(down);
    }

    void updateButtonResources() {
        buttonUpStart = resources.footerButtonUpStart().getURL();
        buttonUpStretch = resources.footerButtonUpStretch().getURL();
        buttonUpEnd = resources.footerButtonUpEnd().getURL();
        buttonDownStart = resources.footerButtonDownStart().getURL();
        buttonDownStretch = resources.footerButtonDownStretch().getURL();
        buttonDownEnd = resources.footerButtonDownEnd().getURL();
    }

    void initButtonHandlers() {
        alertButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (alertButton.getValue()) {
                    eventButton.setValue(false);
                    taskButton.setValue(false);
                    tablePanel.clear();
                    tablePanel.add(alertsTable);

                    firstRowTablePanel.clear();
                    firstRowTablePanel.add(_alertsTable);

                    message.setText("Last Message:");
                    collapseButton.setVisible(false);
                }
                else {
                    alertButton.setValue(true);
                }
            }
        });

        eventButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (eventButton.getValue()) {
                    alertButton.setValue(false);
                    taskButton.setValue(false);
                    tablePanel.clear();
                    tablePanel.add(eventsTable);

                    firstRowTablePanel.clear();
                    firstRowTablePanel.add(_eventsTable);

                    message.setText("Last Message:");
                    collapseButton.setVisible(false);
                }
                else {
                    eventButton.setValue(true);
                }
            }
        });

        taskButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (taskButton.getValue()) {
                    alertButton.setValue(false);
                    eventButton.setValue(false);
                    tablePanel.clear();
                    tablePanel.add(tasksTree);

                    firstRowTablePanel.clear();
                    firstRowTablePanel.add(_tasksTable);

                    message.setText("Last Task:");
                    collapseButton.setVisible(true);
                }
                else {
                    taskButton.setValue(true);
                }
            }
        });

        expandButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String height = widgetPanel.getElement().getParentElement().getParentElement().getStyle().getHeight();
                int offset = 30;
                if (height.equals("30px")) {
                    offset = 162;
                }
                widgetPanel.getElement().getParentElement().getParentElement().getStyle().setHeight(offset, Unit.PX);
                widgetPanel.getElement().getParentElement().getParentElement().getStyle().setBottom(0, Unit.PX);
                Element e =
                        (Element) widgetPanel.getElement()
                                .getParentElement()
                                .getParentElement()
                                .getParentElement()
                                .getChild(2);
                e.getStyle().setBottom(offset, Unit.PX);
                e =
                        (Element) widgetPanel.getElement()
                                .getParentElement()
                                .getParentElement()
                                .getParentElement()
                                .getChild(3);
                e.getStyle().setBottom(offset + 2, Unit.PX);
            }
        });

        collapseButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                tasksTree.collapseAllTasks();
            }
        });
    }

    void initTable(SimpleActionTable<AuditLog> table) {
        table.addColumn(new AuditLogSeverityColumn(), "", "30px");

        TextColumnWithTooltip<AuditLog> logTimeColumn = new FullDateTimeColumn<AuditLog>() {
            @Override
            protected Date getRawValue(AuditLog object) {
                return object.getlog_time();
            }
        };
        table.addColumn(logTimeColumn, "Time", "160px");

        TextColumnWithTooltip<AuditLog> messageColumn = new TextColumnWithTooltip<AuditLog>() {
            @Override
            public String getValue(AuditLog object) {
                return object.getmessage();
            }
        };
        table.addColumn(messageColumn, "Message");
    }

    void initTaskTable(SimpleActionTable<Job> taskTable) {
        ImageResourceColumn<Job> taskStatusColumn = new ImageResourceColumn<Job>() {
            @Override
            public ImageResource getValue(Job object) {
                EntityModel entityModel = new EntityModel();
                entityModel.setEntity(object);
                return new TaskStatusColumn().getValue(entityModel);
            }
        };

        taskTable.addColumn(taskStatusColumn, "Status", "30px");

        FullDateTimeColumn<Job> timeColumn = new FullDateTimeColumn<Job>() {
            @Override
            protected Date getRawValue(Job object) {
                return object.getEndTime() == null ? object.getStartTime() : object.getEndTime();
            }
        };
        taskTable.addColumn(timeColumn, "Time", "160px");

        TextColumnWithTooltip<Job> descriptionColumn = new TextColumnWithTooltip<Job>() {
            @Override
            public String getValue(Job object) {
                return object.getDescription();
            }
        };
        taskTable.addColumn(descriptionColumn, "Description");
    }

    public interface AlertsEventsFooterResources extends CellTable.Resources {
        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/webadmin/css/FooterHeaderlessTable.css" })
        TableStyle cellTableStyle();
    }

    interface Style extends CssResource {

        String barStyle();

        String alertButtonUpStyle();

        String alertButtonDownStyle();

        String eventButtonUpStyle();

        String eventButtonDownStyle();

        String taskButtonUpStyle();

        String taskButtonDownStyle();
    }

    @Override
    public void onTaskCountChange(int count) {
        return;
    }
}
