package com.google.android.apps.forscience.whistlepunk.analytics;

import com.google.android.apps.forscience.whistlepunk.metadata.Label;
import com.google.android.apps.forscience.whistlepunk.metadata.PictureLabel;
import com.google.android.apps.forscience.whistlepunk.metadata.TextLabel;

/**
 * Constants for usage tracking.
 */
public final class TrackerConstants {

    // Screen names.

    public static final String SCREEN_INTRO = "intro";
    public static final String SCREEN_OBSERVE_RECORD = "observe_record";
    public static final String SCREEN_PROJECTS = "projects";
    public static final String SCREEN_PROJECT_DETAIL = "project_detail";
    public static final String SCREEN_NEW_PROJECT = "project_new";
    public static final String SCREEN_UPDATE_PROJECT = "project_update";
    public static final String SCREEN_NEW_EXPERIMENT = "experiment_new";
    public static final String SCREEN_UPDATE_EXPERIMENT = "experiment_update";
    public static final String SCREEN_EXPERIMENT_DETAIL = "experiment_detail";
    public static final String SCREEN_RUN_REVIEW = "run_review";
    public static final String SCREEN_SETTINGS = "settings";
    public static final String SCREEN_DEVICE_MANAGER = "device_manager";
    public static final String SCREEN_DEVICE_OPTIONS = "device_options";
    public static final String SCREEN_UPDATE_RUN = "run_update";

    // Custom dimension indices.

    public static final int DIMENSION_MODE = 1;
    public static final int RELEASE_TYPE = 2;

    // Categories
    public static final String CATEGORY_PROJECTS = "Projects";
    public static final String CATEGORY_EXPERIMENTS = "Experiments";
    public static final String CATEGORY_RUNS = "Runs";
    public static final String CATEGORY_NOTES = "Notes";

    // Event actions
    public static final String ACTION_CREATE = "Create";
    public static final String ACTION_RECORDED = "Recorded";
    public static final String ACTION_EDITED = "EditedValue";
    public static final String ACTION_ARCHIVE = "Archived";
    public static final String ACTION_UNARCHIVE = "Unarchived";
    public static final String ACTION_DELETED = "Deleted";
    public static final String ACTION_DELETE_UNDO = "UndoDelete";

    // Labels
    public static final String LABEL_RECORD = "record";
    public static final String LABEL_RUN_REVIEW = "run_review";
    public static final String LABEL_OBSERVE = "observe";
    public static final String LABEL_EXPERIMENT_DETAIL = "experiment_detail";

    // Values
    public static final long VALUE_TYPE_TEXT = 0;
    public static final long VALUE_TYPE_PICTURE = 1;

    private TrackerConstants() {}

    /**
     * Gets the logging type for a label.
     */
    public static long getLabelValueType(Label label) {
        if (label instanceof PictureLabel) {
            return TrackerConstants.VALUE_TYPE_PICTURE;
        } else if (label instanceof TextLabel) {
            return TrackerConstants.VALUE_TYPE_TEXT;
        } else {
            throw new IllegalArgumentException("Label type is not supported for logging.");
        }
    }
}