/*************************************************************************
*                                                                        *
*  This file is part of the 20n/act project.                             *
*  20n/act enables DNA prediction for synthetic biology/bioengineering.  *
*  Copyright (C) 2017 20n Labs, Inc.                                     *
*                                                                        *
*  Please direct all queries to act@20n.com.                             *
*                                                                        *
*  This program is free software: you can redistribute it and/or modify  *
*  it under the terms of the GNU General Public License as published by  *
*  the Free Software Foundation, either version 3 of the License, or     *
*  (at your option) any later version.                                   *
*                                                                        *
*  This program is distributed in the hope that it will be useful,       *
*  but WITHOUT ANY WARRANTY; without even the implied warranty of        *
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
*  GNU General Public License for more details.                          *
*                                                                        *
*  You should have received a copy of the GNU General Public License     *
*  along with this program.  If not, see <http://www.gnu.org/licenses/>. *
*                                                                        *
*************************************************************************/

package act.installer.metacyc;

import act.installer.metacyc.Resource;
import java.util.Set;
import java.util.HashSet;
import org.json.JSONObject;

public class BPElement {
  protected Resource ID; // getRDFId() in BioPAXElement
  protected String standardName, displayName; // getStandardName(), getDisplayName() in level3.Named
  protected Set<String> name; // getName() in level3.Named
  protected Set<Resource> xrefs; // getXref() in level3.XReferrable
  protected Set<Resource> dataSource; // getDataSource() in level3.Entity
  protected Set<Resource> evidence; // getEvidence() in level3.Observable
  protected Set<String> comment; // getComment in level3.Level3Element

  public BPElement(Resource rdfID, String standardName, String displayName, Set<String> name, Set<Resource> xrefs, Set<Resource> dataSource, Set<Resource> evidence, Set<String> comment) {
    this.ID = rdfID;
    this.standardName = standardName;
    this.displayName = displayName;
    this.name = name;
    this.xrefs = xrefs;
    this.dataSource = dataSource;
    this.evidence = evidence;
    this.comment = comment;
  }

  public BPElement(BPElement e) {
    // convenience function to clone from another
    // DOES NOT do a deep copy
    this.ID = e.ID;
    this.standardName = e.standardName;
    this.displayName = e.displayName;
    this.name = e.name;
    this.xrefs = e.xrefs;
    this.dataSource = e.dataSource;
    this.evidence = e.evidence;
    this.comment = e.comment;
  }

  public Resource getID() { return this.ID; }
  public String getStandardName() { return this.standardName; }
  public Set<String> getName() { return this.name; }
  public Set<Resource> getXrefs() { return this.xrefs; }

  // all subclasses should override this method to allow querying of their fields
  // using OrganismComposition's traverse method
  public Set<Resource> field(NXT typ) {
    return new HashSet<Resource>();
    // there is no need to specifically pull anything using the path coz
    // one can just call the get... accessors and since everything extends
    // BPElement, they would end up looking up the fields here.
  }

  public JSONObject expandedJSON(OrganismComposition src) {
    JsonHelper o = new JsonHelper(src);
    o.add("ID", this.ID.toString());
    o.add("standardName", standardName);
    return o.getJSON();
  }
}
