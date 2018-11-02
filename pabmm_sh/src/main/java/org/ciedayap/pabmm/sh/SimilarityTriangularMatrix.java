/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.pabmm.sh;

/**
 * It implements a similarity triangular matrix using unidimensional mapping to an array of doubles
 * in place of the bidimensional matrix for optimizing the use of the required memory.,
 * @author Mario
 */
public class SimilarityTriangularMatrix {
    /**
     * Total number of rows and columns
     */
    private final Integer dim;   
    /**
     * The unidimensional matrix for storing and retrieving the data
     */
    private final Double umatrix[];
    
    /**
     * Constructor where the dimensio is established for the triangular matrix.
     * @param thedim The dimension
     * @throws Exception An exception could happen when the dimension is null or lesser than 1, or even
     * when the required space for the matrix is not available.
     */
    public SimilarityTriangularMatrix(Integer thedim) throws Exception
    {
        if(thedim==null || thedim<1) throw new Exception("Invalid Dimmension");
        int requiredSpace=computingUnidimensionalMatrixSpace(thedim);
        if(requiredSpace<1) throw new Exception("Invalid space computing");
        dim=thedim;
        umatrix=new Double[requiredSpace];        
    }
    
    /**
     * Default factory method
     * @param thedim The dimension related to the triangular matrix 
     * @return A new instance of the TriangularMatrix
     * @throws Exception it happens when the dimension is invalid or there is not the enough memory space.
     */
    public static SimilarityTriangularMatrix createSimilarityTriangularMatrix(int thedim) throws Exception
    {
        return new SimilarityTriangularMatrix(thedim);
    }
    
    /**
     * It computes the unidimensional total space required for storing the matrix in memory
     * @param dim The wished dimensionality for the matrix
     * @return The total positions required for storing the matrix as an unidimensional array
     */
    protected final static int computingUnidimensionalMatrixSpace(int dim)
    {
        if(dim<1) return 0;                
        return (dim*(dim+1))/2;
    }
    
    /**
     * It calculates the unidimensional position.
     * Mandatory: row must be <= col
     * @param row the number of row between 0 and (dim-1)
     * @param col the number of column between 0 and (dim-1)
     * @return The unidimensional position in the array
     */
    protected final int computingUnidimensionalPosition(int row,int col)
    {
        return ((getDim()*row)+col-((row*(row+1))/2));
    }

        
    /**
     * Set the value on the given row and column into the matrix.
     * @param row A row between 0 and (dim-1)
     * @param col A column between 0 and (dim-1)
     * @param value The value to be incorporated
     * @return TRUE when the value was succesfully incorporated, FALSE otherwise.
     */
    public synchronized boolean set(int row,int col,double value)
    {
        int nrow,ncol;
        if(row>col)
        {
            nrow=col;
            ncol=row;
        }
        else
        {
            nrow=row;
            ncol=col;
        }
        
        if(row<0 || col<0 || row>=getDim() || col>=getDim()) return false;
        
        int position=computingUnidimensionalPosition(nrow,ncol);
        if(position<0 || position>=umatrix.length) return false; 
        
        umatrix[position]=value;
        return true;
    }
    
    /**
     * Get the value on the given row and column into the matrix.
     * @param row A row between 0 and (dim-1)
     * @param col A column between 0 and (dim-1)
     * @return The value at the given row and column. It is possible to obtain a NULL when
     * the position (row and column) has not been initialized.
     */
    public synchronized Double get(int row,int col)
    {
        int nrow,ncol;
        if(row>col)
        {
            nrow=col;
            ncol=row;
        }
        else
        {
            nrow=row;
            ncol=col;
        }
        
        if(row<0 || col<0 || row>=getDim() || col>=getDim()) return null;
        
        int position=computingUnidimensionalPosition(nrow,ncol);
        if(position<0 || position>=umatrix.length) return null; 
        
        return umatrix[position];
    }
    
    /**
     * @return the dimension related to the triangular matrix
     */
    public Integer getDim() {
        return dim;
    }
}
