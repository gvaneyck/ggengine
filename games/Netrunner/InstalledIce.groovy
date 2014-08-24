package Netrunner

class InstalledIce {
    boolean rezzed = false
    Card card

    public InstalledIce() {
    }
    
    public InstalledIce(Card card) {
        this.card = card
    }
    
    public String toString() {
        return "${rezzed ? '^^^' : 'vvv'} ${card} ${rezzed ? '^^^' : 'vvv'}"
    }
}
