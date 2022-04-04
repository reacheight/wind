import React, { useState } from 'react';

import Analysis from './Analysis'
import Header from './Header';
import {FormControl, Input, Spinner} from '@chakra-ui/react';

import styles from '../styles/App.module.css'
import Footer from "./Footer";

const Wind = () => {
  const API_ENDPOINT = process.env.REACT_APP_API_ENDPOINT;

  const [matchId, setMatchId] = useState('')
  const [analysis, setAnalysis] = useState({})
  const [loading, setLoading] = useState(false)
  const [isError, setIsError] = useState(false)
  const [isDownloadError, setIsDownloadError] = useState(false)

  const getAnalysis = event => {
    setAnalysis({})
    setLoading(true)
    setIsError(false)
    setIsDownloadError(false)

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
                  setIsDownloadError(true)
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
      <div className={styles.content}>
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
        {isError && !isDownloadError &&
          <div className={styles.error}> Error occurred :( </div>
        }
        {isDownloadError &&
          <div>
            <div className={styles.error}> Failed to download replay of the match. </div>
            <div className={styles.error}> If it's a recent game, try later. </div>
          </div>
        }
      </div>
      <Footer />
    </div>
  )
}

export default Wind
