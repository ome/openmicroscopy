commit 59b8f9d69f36bcb11fde6fbed0966ead48fa36cb
Author: Josh <josh@glencoesoftware.com>
Date:   Wed Apr 14 13:32:36 2010 +0100

    see #2073 - Working ScriptRepository, but no registration

diff --git a/components/tools/OmeroImporter/src/ome/services/blitz/repo/ScriptRepositoryI.java b/components/tools/OmeroImporter/src/ome/services/blitz/repo/ScriptRepositoryI.java
new file mode 100644
index 0000000..acf8119
--- /dev/null
+++ b/components/tools/OmeroImporter/src/ome/services/blitz/repo/ScriptRepositoryI.java
@@ -0,0 +1,66 @@
+/*
+ *   $Id$
+ *
+ *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
+ *   Use is subject to license terms supplied in LICENSE.txt
+ */
+package ome.services.blitz.repo;
+
+import java.io.File;
+
+import ome.services.blitz.fire.Registry;
+import ome.services.blitz.impl.SharedResourcesI;
+import ome.services.util.Executor;
+import omero.ServerError;
+import omero.model.OriginalFile;
+
+import org.apache.commons.logging.Log;
+import org.apache.commons.logging.LogFactory;
+
+import Ice.Current;
+import Ice.ObjectAdapter;
+
+/**
+ * Repository which makes the included script files available to users.
+ *
+ * @since Beta4.2
+ */
+public class ScriptRepositoryI extends AbstractRepositoryI {
+
+    private final static Log log = LogFactory.getLog(ScriptRepositoryI.class);
+
+    private static String scriptDir() {
+        File current = new File(".");
+        File lib = new File(current, "lib");
+        File scripts = new File(lib, "scripts");
+        return scripts.getAbsolutePath();
+    }
+
+    public ScriptRepositoryI(ObjectAdapter oa, Registry reg, Executor ex,
+            String sessionUuid) {
+        super(oa, reg, ex, sessionUuid, scriptDir());
+    }
+
+    @Override
+    public String generateRepoUuid() {
+        return SharedResourcesI.SCRIPT_REPO;
+    }
+
+    /**
+     */
+    public String getFilePath(final OriginalFile file, Current __current)
+            throws ServerError {
+
+        String url = getFileUrl(file);
+        String uuid = getRepoUuid();
+
+        if (url == null || !url.equals(uuid)) {
+            throw new omero.ValidationException(null, null, url
+                    + " does not belong to this repository: " + uuid);
+        }
+
+        return file.getPath() == null ? null : file.getPath().getValue();
+
+    }
+
+}
