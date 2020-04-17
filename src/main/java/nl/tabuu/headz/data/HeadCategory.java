package nl.tabuu.headz.data;

public enum HeadCategory {
    ALPHABET("https://minecraft-heads.com/scripts/api.php?cat=ALPHABET&tags=true"),
    ANIMALS("https://minecraft-heads.com/scripts/api.php?cat=ANIMALS&tags=true"),
    BLOCKS("https://minecraft-heads.com/scripts/api.php?cat=BLOCKS&tags=true"),
    DECORATION("https://minecraft-heads.com/scripts/api.php?cat=DECORATION&tags=true"),
    FOOD_AND_DRINKS("https://minecraft-heads.com/scripts/api.php?cat=food-drinks&tags=true"),
    HUMANS("https://minecraft-heads.com/scripts/api.php?cat=HUMANS&tags=true"),
    HUMANOID("https://minecraft-heads.com/scripts/api.php?cat=HUMANOID&tags=true"),
    MISCELLANEOUS("https://minecraft-heads.com/scripts/api.php?cat=MISCELLANEOUS&tags=true"),
    MONSTERS("https://minecraft-heads.com/scripts/api.php?cat=MONSTERS&tags=true"),
    PLANTS("https://minecraft-heads.com/scripts/api.php?cat=PLANTS&tags=true");

    private String _url;

    HeadCategory(String url){
        _url = url;
    }

    public String getURL(){
        return _url;
    }
}
