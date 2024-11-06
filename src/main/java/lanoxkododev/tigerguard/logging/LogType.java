package lanoxkododev.tigerguard.logging;

/**
 * LogType enums to be used for marking system print statemtns with a certain level.
 * These are encased in square brackets '[]' and have all statements for it's log event appended afterwards on the same/following lines depending on the log format entered and constraints.
 */
public enum LogType {

	DIAGNOSTIC, INFO, WARNING, DEBUG, ERROR,
	DATABASE_INFO, DATABASE_WARNING, DATABASE_ERROR,
	XP_DATABASE_INFO, XP_DATABASE_WARNING, XP_DATABASE_ERROR,
	RANK_INFO, RANK_WARNING, RANK_ERROR;
}