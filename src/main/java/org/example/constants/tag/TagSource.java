package org.example.constants.tag;

// 搜索存储类型
public enum TagSource {
    NATIVE(0, "native"),
    SIM51(1, "51sim"),
    ADMIN(2, "admin");


    private final int code;
    private final String name;

    TagSource(int code, String name){
        this.code = code;
        this.name = name;
    }

    public int getCode(){
        return code;
    }

    public String getName(){
        return name;
    }

    public static TagSource getFromName(String name){
        for(TagSource tmp : TagSource.values()){
            if(tmp.name.equals(name)){
                return tmp;
            }
        }
        return null;
    }

    public static TagSource getFromCode(int code){
        for(TagSource tmp : TagSource.values()){
            if(tmp.code == code){
                return tmp;
            }
        }
        return null;
    }
}
