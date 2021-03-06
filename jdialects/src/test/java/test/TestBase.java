/*
 * jDialects, a tiny SQL dialect tool
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later. See
 * the lgpl.txt file in the root directory or
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package test;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jdialects.Dialect;
import com.github.drinkjava2.jdialects.model.TableModel;

import test.DataSourceConfig.DataSourceBox;
import test.utils.tinyjdbc.TinyJdbc;

/**
 * This base test class in charge of configure and close data sources.
 * 
 * @author Yong Z.
 * @since 1.0.2
 *
 */
public class TestBase {
	protected DataSource ds = BeanBox.getBean(DataSourceBox.class);
	protected TinyJdbc dbPro = new TinyJdbc(ds);
	protected Dialect guessedDialect = Dialect.guessDialect(ds);

	@Before
	public void initDao() {
		System.out.println("Current guessedDialect=" + guessedDialect);
		// db.setAllowShowSQL(true);
	}

	@After
	public void closeDataSource() {
		BeanBox.defaultContext.close();// close dataSource
	}

	protected static void printDDLs(String[] ddl) {
		for (String str : ddl) {
			System.out.println(str);
		}
	}

	protected void quietExecuteDDLs(String... ddls) {
		for (String sql : ddls) {
			try {
				dbPro.nExecute(sql);
			} catch (Exception e) {
			}
		}
	}

	protected void executeDDLs(String... sqls) {
		for (String sql : sqls)
			dbPro.nExecute(sql);
	}

	public void reBuildDB(TableModel... tables) {
		String[] ddls = guessedDialect.toDropDDL(tables);
		quietExecuteDDLs(ddls);

		ddls = guessedDialect.toCreateDDL(tables);
		executeDDLs(ddls);
	}

	protected void testOnCurrentRealDatabase(TableModel... tables) {
		System.out.println("======Test on real Database of dialect: " + guessedDialect + "=====");

		String[] ddls = guessedDialect.toDropDDL(tables);

		quietExecuteDDLs(ddls);

		ddls = guessedDialect.toCreateDDL(tables);
		executeDDLs(ddls);

		ddls = guessedDialect.toDropAndCreateDDL(tables);
		executeDDLs(ddls);

		ddls = guessedDialect.toDropDDL(tables);
		executeDDLs(ddls);
	}

	protected static void printOneDialectsDDLs(Dialect dialect, TableModel... tables) {
		System.out.println("======" + dialect + "=====");
		try {
			String[] ddls = dialect.toDropAndCreateDDL(tables);
			printDDLs(ddls);
			// printDDLs(DDLFormatter.format(ddls));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception found: " + e.getMessage());
		}
	}

	protected static void printAllDialectsDDLs(TableModel... tables) {
		Dialect[] diaList = Dialect.values();
		for (Dialect dialect : diaList) {
			System.out.println("======" + dialect + "=====");
			try {
				String[] ddls = dialect.toDropAndCreateDDL(tables);
				printDDLs(ddls);
				// printDDLs(DDLFormatter.format(ddls));
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Exception found: " + e.getMessage());
			}
		}
	}
}
