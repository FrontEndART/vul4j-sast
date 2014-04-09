package net.onrc.onos.core.util;

import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * The class representing multiple Flow Entry actions.
 * <p/>
 * A set of Flow Entry actions need to be applied to each packet.
 */
public class FlowEntryActions {
    private ArrayList<FlowEntryAction> actions;    // The Flow Entry Actions

    /**
     * Default constructor.
     */
    public FlowEntryActions() {
        actions = new ArrayList<FlowEntryAction>();
    }

    /**
     * Constructor from a string.
     * <p/>
     * The string has the following form:
     * [[type=XXX action=XXX];[type=XXX action=XXX];...;]
     *
     * @param actionsStr the set of actions as a string.
     */
    public FlowEntryActions(String actionsStr) {
        this.fromString(actionsStr);
    }

    /**
     * Copy constructor.
     *
     * @param other the object to copy from.
     */
    public FlowEntryActions(FlowEntryActions other) {
        actions = new ArrayList<FlowEntryAction>();

        for (FlowEntryAction action : other.actions) {
            FlowEntryAction newAction = new FlowEntryAction(action);
            actions.add(newAction);
        }
    }

    /**
     * Get the Flow Entry Actions.
     *
     * @return the Flow Entry Actions.
     */
    @JsonProperty("actions")
    public ArrayList<FlowEntryAction> actions() {
        return actions;
    }

    /**
     * Set the Flow Entry Actions.
     *
     * @param actions the Flow Entry Actions to set.
     */
    @JsonProperty("actions")
    public void setActions(ArrayList<FlowEntryAction> actions) {
        this.actions = actions;
    }

    /**
     * Add a Flow Entry Action.
     *
     * @param flowEntryAction the Flow Entry Action to add.
     */
    public void addAction(FlowEntryAction flowEntryAction) {
        actions.add(flowEntryAction);
    }

    /**
     * Test whether the set of actions is empty.
     *
     * @return true if the set of actions is empty, otherwise false.
     */
    @JsonIgnore
    public Boolean isEmpty() {
        return actions.isEmpty();
    }

    /**
     * Convert the set of actions to a string.
     * <p/>
     * The string has the following form:
     * [[type=XXX action=XXX];[type=XXX action=XXX];...;]
     *
     * @return the set of actions as a string.
     */
    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();

        ret.append("[");
        for (FlowEntryAction action : actions) {
            ret.append(action.toString() + ";");
        }
        ret.append("]");

        return ret.toString();
    }

    /**
     * Convert a string to a set of actions.
     * <p/>
     * The string has the following form:
     * [[type=XXX action=XXX];[type=XXX action=XXX];...;]
     *
     * @param actionsStr the set of actions as a string.
     */
    public void fromString(String actionsStr) {
        String decode = actionsStr;

        actions = new ArrayList<FlowEntryAction>();

        if (decode.isEmpty())
            return;        // Nothing to do

        // Remove the '[' and ']' in the beginning and the end of the string
        if ((decode.length() > 1) && (decode.charAt(0) == '[') &&
                (decode.charAt(decode.length() - 1) == ']')) {
            decode = decode.substring(1, decode.length() - 1);
        } else {
            throw new IllegalArgumentException("Invalid action string");
        }

        // Split the string, and decode each action
        String[] parts = decode.split(";");
        for (int i = 0; i < parts.length; i++) {
            decode = parts[i];
            if ((decode == null) || decode.isEmpty())
                continue;
            FlowEntryAction flowEntryAction = null;
            try {
                flowEntryAction = new FlowEntryAction(decode);
            } catch (IllegalArgumentException e) {
                // TODO: Ignore invalid actions for now
                continue;
            }
            if (flowEntryAction != null)
                actions.add(flowEntryAction);
        }
    }
}
