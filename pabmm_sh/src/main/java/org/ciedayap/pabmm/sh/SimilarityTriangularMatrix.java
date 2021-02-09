/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ciedayap.pabmm.sh;

import java.util.ArrayList;
import java.util.Arrays;
import org.apache.storm.shade.org.apache.commons.lang.ArrayUtils;

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
     * Constructor where the dimension is established for the triangular matrix.
     * @param thedim The dimension will produce a dimxdim triangular matrix
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
     * This constructor replicates the matrix in a different instance.
     * @param thedim The dimension
     * @param umat matrix to be copied
     * @throws Exception An exception could happen when the dimension is null or lesser than 1, or even
     * when the required space for the matrix is not available.
     */
    public SimilarityTriangularMatrix(Integer thedim,double umat[]) throws Exception
    {
        if(thedim==null || thedim<1) throw new Exception("Invalid Dimmension");        
        if(umat==null || umat.length<1) throw new Exception("Invalid Matrix");
        int requiredSpace=computingUnidimensionalMatrixSpace(thedim);
        if(requiredSpace<1) throw new Exception("Invalid space computing");
        if(requiredSpace!=umat.length) throw new Exception("There is not coincidence between the matrix and required space");
        
        dim=thedim;
        umatrix=new Double[requiredSpace];        
        for(int i=0;i<umatrix.length;i++) umatrix[i]=umat[i];            
    }

    /**
     * Constructor where the dimensio is established for the triangular matrix.
     * @param thedim The dimension
     * @param value A value to initialize the matrix
     * @throws Exception An exception could happen when the dimension is null or lesser than 1, or even
     * when the required space for the matrix is not available.
     */
    public SimilarityTriangularMatrix(Integer thedim, Double value) throws Exception
    {
        if(thedim==null || thedim<1) throw new Exception("Invalid Dimmension");
        if(value==null) throw new Exception("Invalid value");
        
        int requiredSpace=computingUnidimensionalMatrixSpace(thedim);
        if(requiredSpace<1) throw new Exception("Invalid space computing");
        dim=thedim;
        umatrix=new Double[requiredSpace];        
        reinitialize(value);
    }

    /**
     * It generates a hard copy from the original matrix
     * @return a new instance without any dependence on the original instance
     * @throws Exception It is raised when some abnormality is detected in the constructor
     */
    public synchronized SimilarityTriangularMatrix duplicate() throws Exception
    {
        if(umatrix==null || umatrix.length==0) return null;
        
        double[] umat=ArrayUtils.toPrimitive(umatrix);
        
        return new SimilarityTriangularMatrix(this.dim,umat);
    }
    
    /**
     * This method returns a copy of the matrix dividing each element by the "value"
     * @param value The value to be used to divide the matrix (it must be different from zero)
     * @return It returns a copy of the original matrix dividing each element by "value". Null is returned 
     * when there not exists matrix or the value is zero
     * @throws Exception 
     */
    public synchronized SimilarityTriangularMatrix dividedBy(double value) throws Exception
    {
        if(umatrix==null || umatrix.length==0) return null;
        if(value==0) return null;
        
        double[] umat=ArrayUtils.toPrimitive(umatrix);
        for(int i=0;i<umat.length;i++)
            umat[i]= umat[i]/value;
        
        return new SimilarityTriangularMatrix(this.dim,umat);
        
    }
    /**
     * Default factory method
     * @param thedim The dimension related to the triangular matrix 
     * @return A new instance of the TriangularMatrix
     * @throws Exception it happens when the dimension is invalid or there is not the enough memory space.
     */
    public synchronized static SimilarityTriangularMatrix createSimilarityTriangularMatrix(int thedim) throws Exception
    {
        return new SimilarityTriangularMatrix(thedim);
    }

    /**
     * Default factory method with initialization
     * @param thedim The dimension related to the triangular matrix 
     * @param value A double value that will be used to initialize the matrix
     * @return A new instance of the TriangularMatrix
     * @throws Exception it happens when the dimension is invalid or there is not the enough memory space.
     */
    public synchronized static SimilarityTriangularMatrix createSimilarityTriangularMatrix(int thedim, Double value) throws Exception
    {
        return new SimilarityTriangularMatrix(thedim,value);
    }
    
    /**
     * restart the matrix's cells to a given value
     * @param value The value to initialize the 
     * @return TRUE when it was reinitialized, FALSE otherwise.
     */
    public final synchronized boolean reinitialize(Double value)
    {
        if(value==null) return false;
        
        Arrays.fill(umatrix, value);
        return true;
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
     * It returns the vector related with the indicated row
     * @param row The row 
     * @return The vector related to the row into the matrix, null otherwise
     */
    public synchronized ArrayList<Double> getRow(int row)
    {
        if(row<0 || row>=getDim()) return null;
        
        ArrayList<Double> ret=new ArrayList();
        for(int i=0;i<dim;i++)
            ret.add(get(row,i));
        
        return ret;
    }
    
    /**
     * @return the dimension related to the triangular matrix
     */
    public Integer getDim() {
        return dim;
    }
}
