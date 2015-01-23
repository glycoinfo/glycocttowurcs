package org.eurocarbdb.MolecularFramework.io.WURCS;


/**
 * Class for MAP code conversion from Substituent name in GlycoCT
 * @author MasaakiMatsubara
 *
 */
public enum SubstituentTypeToMAP {
    ACETYL("acetyl", "CC/2=O", null, null),
    AMINO("amino", "N", "N*", null),
    ANHYDRO("anhydro", "", null, null),
    BROMO("bromo", "B", null, null),
    CHLORO("chloro", "Cl", null, null),
    EPOXY("epoxy", "", null, null), // 0
    ETHANOLAMINE("ethanolamine", "CCN", "CCN*", false),
    ETHYL("ethyl","CC", null, null),
    FLOURO("fluoro", "F", null, null),
    FORMYL("formyl", "C=O", null, null),
    GLYCOLYL("glycolyl", "CCO/2=O", null, null),
    HYDROXYMETHYL("hydroxymethyl", "CO", null, null),
    IMINO("imino", "=N", "=N*", false),
    IODO("iodo", "I", null, null),
    LACTONE("lactone", "", null, null), // 0
    METHYL("methyl", "C", null, null),
    N_ACETYL("n-acetyl", "NCC/3=O", null, null),
    N_ALANINE("n-alanine", "NCC^XC/4N/3=O", null, null),
    N_DIMETHYL("n-dimethyl", "NC/2C", null, null),
    N_FORMYL("n-formyl", "NC=O", null, null),
    N_GLYCOLYL("n-glycolyl", "NCCO/3=O", null, null),
    N_METHYL("n-methyl", "NC", null, null),
    N_SUCCINATE("n-succinate", "NCCCCO/6=O/3=O", null, null),
    SUCCINATE("succinate", "CCCCO/5=O/2=O", "CCCC*/5=O/2=O", null),
    N_SULFATE("n-sulfate", "NSO/3=O/3=O", "NS*/3=O/3=O", true),
    N_TRIFLOUROACETYL("n-triflouroacetyl", "NCCF/4F/4F/3=O", null, null),
    NITRATE("nitrate", "C=O/2=O", null, null),
    PHOSPHATE("phosphate", "PO/2O/2=O", "P^X*/2O/2=O", null),
    PYRUVATE("pyruvate", "C^X*/2CO/4=O/2C", null, null),  // sometime there is no chirality
    PYROPHOSPHATE("pyrophosphate", "P^XOPO/4O/4=O/2O/2=O", "P^XOP^X*/4O/4=O/2O/2=O", null),
    TRIPHOSPHATE("triphosphate", "P^XOP^XOPO/6O/6=O/4O/4=O/2O/2=O", "P^XOP^XOP^X*/6O/6=O/4O/4=O/2O/2=O", null),
    R_LACTATE("(r)-lactate", "CC^RC/3O/2=O", null, null),
    R_PYRUVATE("(r)-pyruvate","C^R*/2CO/4=O/2C/", null, null),
    S_LACTATE("(s)-lactate", "CC^SC/3O/2=O", null, null),
    S_PYRUVATE("(s)-pyruvate","C^S*/2CO/4=O/2C/", null, null),
    SULFATE("sulfate", "SO/2=O/2=O", "S*/2=O/2=O", null),
    THIO("thio", "S", null, null),
    AMIDINO("amidino", "CN/2=N", null, null),
    N_AMIDINO("n-amidino", "NCN/3=N", null, null),
    R_CARBOXYMETHYL("(r)-carboxymethyl", "?*", null, null), // no chirality
    CARBOXYMETHYL("carboxymethyl", "CCO/3=O", null, null),
    S_CARBOXYMETHYL("(s)-carboxymethyl", "?*", null, null), // no chirality
    R_CARBOXYETHYL("(r)-carboxyethyl", "C^RCO/3=O/2C" , null, null),
    S_CARBOXYETHYL("(s)-carboxyethyl","C^SCO/3=O/2C", null, null),
    N_METHYLCARBAMOYL("n-methyl-carbamoyl", "CNC/2=O", null, null),
    PHOSPHO_ETHANOLAMINE("phospho-ethanolamine", "P^XOCCN/2O/2=O", "NCCOP^X*/6O/6=O", true),
    DIPHOSPHO_ETHANOLAMINE("diphospho-ethanolamine","P^XOP^XOCCN/4O/4=O/2O/2=O", "NCCOP^XOP^X*/8O/8=O/6O/6=O", true),
    PHOSPHO_CHOLINE("phospho-choline", "P^XOCCN/5N/5N/2O/2=O", null, null),
    X_LACTATE("(x)-lactate", "CC^XC/3O/2=O", null, null),
//    X_PYRUVATE("(x)-pyruvate","--"),
    R_1_HYDROXYMETHYL("(r)-1-hydroxymethyl", "?*", null, null), // no chirality
    S_1_HYDROXYMETHYL("(s)-1-hydroxymethyl", "?*", null, null); // no chirality

    private String m_strName;
    private String m_strMAPforBMU;
    private String m_strMAPforMLU;
    private Boolean m_bIsSwapCarbonPositions;

    /**
     * private constructor
     * @param a_strName				substituent name
     * @param a_strMAPforBMU		MAP code for BMU
     * @param a_strMAPforMLU		MAP code for MLU
     * @param a_bIsSwapCarbonPosition	MAP code for MLU is swap carbon position
     */
    private SubstituentTypeToMAP( String a_strName, String a_strMAPforBMU, String a_strMAPforMLU, Boolean a_bIsSwapCarbonPosition )
    {
        this.m_strName = a_strName;
        this.m_strMAPforBMU = a_strMAPforBMU;
        this.m_strMAPforMLU = a_strMAPforMLU;
        this.m_bIsSwapCarbonPositions = a_bIsSwapCarbonPosition;
    }

    public static SubstituentTypeToMAP forName( String a_strName )
    {
        String t_strName = a_strName.toUpperCase();
        for ( SubstituentTypeToMAP t_objType : SubstituentTypeToMAP.values() )
        {
            if ( t_objType.m_strName.equalsIgnoreCase(t_strName) )
            {
                return t_objType;
            }
        }
        return null;
    }

    public String getName()
    {
        return this.m_strName;
    }

    public String getMAPforBMU()
    {
        return this.m_strMAPforBMU;
    }

    public String getMAPforMLU()
    {
        return this.m_strMAPforMLU;
    }

    public Boolean isSwapCarbonPositions() {
    	return this.m_bIsSwapCarbonPositions;
    }
}
