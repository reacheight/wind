import React, { useState } from 'react';

import Analysis from './Analysis/Analysis'
import Header from './Header/Header';
import {FormControl, Input, Spinner} from '@chakra-ui/react';

import styles from './App.module.css'

const Wind = () => {
  const API_ENDPOINT = process.env.REACT_APP_API_ENDPOINT;

  const [matchId, setMatchId] = useState('')
  const [analysis, setAnalysis] = useState({})
  const [loading, setLoading] = useState(false)
  const [isError, setIsError] = useState(false)

  const getAnalysis = event => {
    setAnalysis({})
    setLoading(true)
    setIsError(false)

    fetch(`${API_ENDPOINT}/analysis/${matchId}`, { method: 'POST' })
      .then(response => {
        if (!response.ok) {
          setIsError(true)
          setLoading(false)
        }
        else {
          let timer = setInterval(() => {
            fetch(`${API_ENDPOINT}/analysis/${matchId}/state`)
              .then(stateResponse => {
                if (!stateResponse.ok) {
                  setIsError(true)
                  setLoading(false)
                  clearInterval(timer)
                }

                return stateResponse.json()
              })
              .then(state => {
                if (state.status === 1) {
                  fetch(`${API_ENDPOINT}/analysis/${matchId}`)
                    .then(analysisResponse => analysisResponse.json())
                    .then(json => {
                      setAnalysis(json)
                      setLoading(false)
                      clearInterval(timer)
                    })
                }
                if (state.status === 2) {
                  setIsError(true)
                  setLoading(false)
                  clearInterval(timer)
                }
              })
          }, 5000)
        }
      })
      .catch(e => {
        setLoading(false)
        setIsError(true)
      })
      
    event.preventDefault()
  }

  return (
    <div className={styles.app}>
      <Header />
      <form className={styles.input} onSubmit={getAnalysis}>
        <FormControl>
          <Input id='matchId' value={matchId} type='text' placeholder='Enter match id' onChange={event => setMatchId(event.target.value)} />
        </FormControl>
      </form>
      {loading &&
        <div className={styles.spinner}>
          <Spinner />
        </div>
      }
      <Analysis analysis={analysis} />
      {isError &&
        <div className={styles.error}> Error occurred :( </div>
      }
    </div>
  )
}

export default Wind
