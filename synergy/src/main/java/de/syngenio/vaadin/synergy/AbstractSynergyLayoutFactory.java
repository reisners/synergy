package de.syngenio.vaadin.synergy;

public abstract class AbstractSynergyLayoutFactory implements SynergyLayoutFactory
{
    private SynergyLayoutFactory subitemLayoutFactory = this;
    protected String styleName = null;
    protected boolean compactArrangement = true;

    public boolean isCompactArrangement()
    {
        return compactArrangement;
    }

    public void setCompactArrangement(boolean compactArrangement)
    {
        this.compactArrangement = compactArrangement;
    }

    public void setSubitemLayoutFactory(SynergyLayoutFactory subitemLayoutFactory)
    {
        this.subitemLayoutFactory = subitemLayoutFactory;
    }

    @Override
    public SynergyLayoutFactory getSubitemLayoutFactory()
    {
        return subitemLayoutFactory;
    }

    @Override
    public void setStyleName(String styleName)
    {
        this.styleName = styleName;
    }

    @Override
    public String generateStyleName() {
        if (styleName != null) {
            return "synergy-"+styleName;
        } else {
            return "synergy";
        }
    }
}