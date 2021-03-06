/**
 * Copyright (C) 2016 Yong Zhu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package test.functiontest.jdialects;

import org.junit.Assert;
import org.junit.Test;

import com.github.drinkjava2.jdialects.Type;
import com.github.drinkjava2.jdialects.annotation.jpa.GenerationType;
import com.github.drinkjava2.jdialects.id.AutoIdGenerator;
import com.github.drinkjava2.jdialects.id.IdGenerator;
import com.github.drinkjava2.jdialects.id.SortedUUIDGenerator;
import com.github.drinkjava2.jdialects.id.UUID25Generator;
import com.github.drinkjava2.jdialects.id.UUID32Generator;
import com.github.drinkjava2.jdialects.id.UUID36Generator;
import com.github.drinkjava2.jdialects.model.TableModel;

import test.TestBase;

/**
 * Unit test for SortedUUIDGenerator
 */
public class IdgeneratorTest extends TestBase {

	@Test
	public void testPKey() {// nextID
		TableModel t = new TableModel("testPKey");
		t.column("id1").STRING(25).pkey();
		Assert.assertTrue(t.column("id1").getPkey());
	}

	@Test
	public void testUUIDs() {// nextID
		TableModel t = new TableModel("testNextIdTable");
		t.column("id1").STRING(25).pkey();
		t.column("id2").STRING(32);
		t.column("id3").STRING(36);
		String[] ddls = guessedDialect.toDropDDL(t);
		quietExecuteDDLs(ddls);

		ddls = guessedDialect.toCreateDDL(t);
		executeDDLs(ddls);
		for (int i = 0; i < 10; i++) {
			Object id1 = guessedDialect.getNexID(UUID25Generator.INSTANCE, dbPro, null);
			Object id2 = guessedDialect.getNexID(UUID32Generator.INSTANCE, dbPro, null);
			Object id3 = guessedDialect.getNexID(UUID36Generator.INSTANCE, dbPro, null);
			System.out.println(id1);
			System.out.println(id2);
			System.out.println(id3);
			Assert.assertTrue(("" + id1).length() == 25);
			Assert.assertTrue(("" + id2).length() == 32);
			Assert.assertTrue(("" + id3).length() == 36);
			dbPro.nExecute("insert into testNextIdTable (id1,id2,id3) values(?,?,?) ", id1, id2, id3);
		}
	}

	@Test
	public void testAutoIdGenerator() {
		TableModel table = new TableModel("testAutoIdGenerator");
		table.column("id").STRING(30).pkey().autoId();
		reBuildDB(table);

		IdGenerator gen = table.getColumn("id").getIdGenerator();
		for (int i = 0; i < 5; i++)
			System.out.println(gen.getNextID(dbPro, guessedDialect, null));

		gen = AutoIdGenerator.INSTANCE;
		for (int i = 0; i < 5; i++)
			System.out.println(gen.getNextID(dbPro, guessedDialect, null));
	}

	@Test
	public void testSortedUUIDGenerator() {
		TableModel table = new TableModel("testSortedUUIDGenerator");
		table.sortedUUIDGenerator("sorteduuid", 8, 8);
		table.addGenerator(new SortedUUIDGenerator("sorteduuid2", 10, 10));
		table.column("id").STRING(30).pkey().idGenerator("sorteduuid");
		table.column("id2").STRING(30).pkey().idGenerator("sorteduuid2");
		reBuildDB(table);

		IdGenerator gen1 = table.getIdGenerator("sorteduuid");
		for (int i = 0; i < 10; i++)
			System.out.println(gen1.getNextID(dbPro, guessedDialect, null));

		IdGenerator gen2 = table.getIdGenerator("sorteduuid2");
		for (int i = 0; i < 10; i++)
			System.out.println(gen2.getNextID(dbPro, guessedDialect, null));
	}

	@Test
	public void testSequenceIdGenerator() {
		if (!guessedDialect.getDdlFeatures().supportBasicOrPooledSequence())
			return;
		TableModel table1 = new TableModel("testTableIdGenerator");
		table1.sequenceGenerator("seq1", "seq1", 1, 10);
		table1.column("id").STRING(30).pkey().idGenerator("seq1");
		table1.column("id2").STRING(30).pkey().sequenceGenerator("seq2", "seq2", 1, 20);

		TableModel table2 = new TableModel("testTableIdGenerator2");
		table2.sequenceGenerator("seq3", "seq3", 1, 10);
		table2.column("id").STRING(30).pkey().idGenerator("seq3");
		table2.column("id2").STRING(30).pkey().sequenceGenerator("seq2", "seq2", 1, 20);

		reBuildDB(table1, table2);

		IdGenerator gen1 = table1.getIdGenerator("seq1");
		IdGenerator gen2 = table1.getIdGenerator("seq2");
		for (int i = 0; i < 3; i++) {
			System.out.println(gen1.getNextID(dbPro, guessedDialect, null));
			System.out.println(gen2.getNextID(dbPro, guessedDialect, null));
		}

		IdGenerator gen3 = table2.getIdGenerator("seq3");
		IdGenerator gen4 = table2.getIdGenerator("seq2");
		for (int i = 0; i < 3; i++) {
			System.out.println(gen3.getNextID(dbPro, guessedDialect, null));
			System.out.println(gen4.getNextID(dbPro, guessedDialect, null));
		}
	}

	@Test
	public void testTableIdGenerator() {
		TableModel table1 = new TableModel("testTableIdGenerator");
		table1.tableGenerator("tab1", "tb1", "pkCol", "valueColname", "pkColVal", 1, 10);
		table1.column("id").STRING(30).pkey().idGenerator("tab1");
		table1.column("id2").STRING(30).pkey().tableGenerator("tab2", "tb1", "pkCol", "valueColname", "pkColVal", 1,
				10);

		TableModel table2 = new TableModel("testTableIdGenerator2");
		table2.tableGenerator("tab3", "tb1", "pkCol", "valueColname", "pkColVal", 1, 10);
		table2.column("id").STRING(30).pkey().idGenerator("tab3");
		table2.column("id2").STRING(30).pkey().tableGenerator("tab2", "tb1", "pkCol", "valueColname", "pkColVal", 1,
				10);

		reBuildDB(table1, table2);

		IdGenerator gen1 = table1.getIdGenerator("tab1");
		IdGenerator gen2 = table1.getIdGenerator("tab2");
		for (int i = 0; i < 3; i++) {
			System.out.println(gen1.getNextID(dbPro, guessedDialect, null));
			System.out.println(gen2.getNextID(dbPro, guessedDialect, null));
		}

		IdGenerator gen3 = table2.getIdGenerator("tab3");
		IdGenerator gen4 = table2.getIdGenerator("tab2");
		for (int i = 0; i < 3; i++) {
			System.out.println(gen3.getNextID(dbPro, guessedDialect, null));
			System.out.println(gen4.getNextID(dbPro, guessedDialect, null));
		}
	}

	@Test
	public void testIdentityGenerator() {
		TableModel table = new TableModel("testIdentity");
		table.column("id").INTEGER().identityId();
		table.column("name").STRING(30);
		reBuildDB(table);

		dbPro.nExecute("insert into testIdentity (name) values(?)", "Tom");
		dbPro.nExecute("insert into testIdentity (name) values(?)", "Sam");
		IdGenerator idGen = table.getIdGenerator(GenerationType.IDENTITY);
		System.out.println(idGen.getNextID(dbPro, guessedDialect, Type.INTEGER));

		idGen = table.getColumn("id").getIdGenerator();
		System.out.println(idGen.getNextID(dbPro, guessedDialect, Type.INTEGER));
	}

}
