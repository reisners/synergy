/***********************************************************************
 * (C)opyright 2014 by syngenio AG München, Germany
 * [All rights reserved]. This product and related documentation are
 * protected by copyright restricting its use, copying, distribution,
 * and decompilation. No part of this product or related documentation
 * may be reproduced in any form by any means without prior written
 * authorization of syngenio or its partners, if any. Unless otherwise
 * arranged, third parties may not have access to this product or
 * related documentation.
 **********************************************************************/

/***********************************************************************
 *    $Author$
 *   $RCSfile$
 *  $Revision$
 *        $Id$
 **********************************************************************/

package de.syngenio.xps;

import java.util.ArrayList;
import java.util.List;

import de.syngenio.xps.XPS.Record;

public class RecordLogger
{
    private List<Record> records = new ArrayList<Record>();
    
    public void add(Record record)
    {
        records.add(record);
    }

    protected List<Record> getRecords()
    {
        return records;
    }
}
