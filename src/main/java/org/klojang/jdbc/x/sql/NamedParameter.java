package org.klojang.jdbc.x.sql;

import org.klojang.util.collection.IntList;

/**
 * <p>Represents a single named parameter within a SQL statement. The {@code positions}
 * component of this record contains the positions of the parameter within the SQL
 * statement. For example, in the following (nonsensical) query:
 *
 * <blockquote><pre>{@code
 * SELECT *
 *   FROM person
 *  WHERE first_name = :firstName
 *     OR last_name = :lastName
 *     OR full_name = :fullName
 *     OR full_name LIKE :firstName
 *     OR full_name LIKE :lastName
 * }</pre></blockquote>
 *
 * <p>the {@code firstName} parameter occurs at positions 0 and 3, the {@code firstName}
 * parameter occurs at positions 1 and 4, and the {@code fullName} parameter occurs at
 * position 2.
 *
 * @param name the name of the parameters
 * @param positions the positions at which the parameter can be found.
 */
public record NamedParameter(String name, IntList positions) { }
