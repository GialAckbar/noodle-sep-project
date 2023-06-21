package shared;

import java.io.File;

public class KategorieDatei extends LVKategorieElement {
    private String name = null;
    private String path = null;
    File file = null;
    int dateiid = -1;

    public KategorieDatei(int id, String anzeigename, int position) {
        super(anzeigename, position);
        dateiid = id;
        this.anzeigename = anzeigename;
    }
    public KategorieDatei(int id, String anzeigename) {
        super(anzeigename);
        dateiid = id;
        this.anzeigename = anzeigename;
    }
    public KategorieDatei(String anzeigename, String name, String path) {
        super(anzeigename);
        this.name = name;
        this.path = path;
    }
    public KategorieDatei(String anzeigename, String name, String path, File file) {
        super(anzeigename);
        this.name = name;
        this.path = path;
        this.file = file;
    }

    public void setDateiid(int dateiid) {
        this.dateiid = dateiid;
    }
    public int getDateiid() {
        return dateiid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public File getFile() {
        return file;
    }
    public void setFile(File file) {
        this.file = file;
    }
    public String getPathWithName () {
        if (path != null && name != null) {
            return (path + name);
        } else {
            return "";
        }

    }
}
